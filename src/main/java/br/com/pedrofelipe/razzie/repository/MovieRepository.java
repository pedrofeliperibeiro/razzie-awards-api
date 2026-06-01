package br.com.pedrofelipe.razzie.repository;

import br.com.pedrofelipe.razzie.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * Apenas filmes vencedores importam para o cálculo de intervalos, então
     * deixamos o banco filtrá-los em vez de carregar todas as linhas e filtrar
     * em memória.
     */
    List<Movie> findByWinnerTrue();
}
