package br.com.pedrofelipe.razzie.csv;

import br.com.pedrofelipe.razzie.model.Movie;
import br.com.pedrofelipe.razzie.repository.MovieRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Carrega o CSV de filmes no banco assim que o contexto da aplicação está pronto.
 *
 * <p>Roda como um {@link CommandLineRunner}, então executa depois que o contexto do
 * Spring (e a camada JPA) está totalmente inicializado. A carga é idempotente: não
 * faz nada se a tabela já contém dados.
 */
@Component
public class MovieCsvLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(MovieCsvLoader.class);

    private final MovieRepository movieRepository;
    private final String csvFileName;

    public MovieCsvLoader(MovieRepository movieRepository,
                          @Value("${app.movies.csv}") String csvFileName) {
        this.movieRepository = movieRepository;
        this.csvFileName = csvFileName;
    }

    @Override
    public void run(String... args) {
        if (movieRepository.count() > 0) {
            log.info("Filmes já carregados; ignorando importação do CSV.");
            return;
        }

        List<Movie> movies = readMovies();
        movieRepository.saveAll(movies);
        log.info("Carregados {} filmes de {}.", movies.size(), csvFileName);
    }

    private List<Movie> readMovies() {
        ClassPathResource resource = new ClassPathResource(csvFileName);

        try (InputStream in = resource.getInputStream();
             Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setDelimiter(';')
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {

            List<Movie> movies = new ArrayList<>();
            for (CSVRecord record : parser) {
                movies.add(toMovie(record));
            }
            return movies;

        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o CSV de filmes: " + csvFileName, e);
        }
    }

    private Movie toMovie(CSVRecord record) {
        return new Movie(
                Integer.parseInt(record.get("year")),
                record.get("title"),
                record.get("studios"),
                record.get("producers"),
                "yes".equalsIgnoreCase(record.get("winner"))
        );
    }
}
