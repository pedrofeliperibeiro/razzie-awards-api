package br.com.pedrofelipe.razzie.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Filme indicado ao prêmio de Pior Filme do Golden Raspberry Awards.
 *
 * <p>Esta entidade é uma representação fiel de uma linha do CSV: o campo
 * {@code producers} mantém a string crua, sem parsing, exatamente como foi
 * ingerida. Quebrá-la em produtores individuais é uma responsabilidade de domínio,
 * tratada no cálculo dos intervalos — não de persistência.
 *
 * <p>Lombok gera apenas os getters e o construtor sem-args exigido pelo JPA. Não
 * usamos {@code @Data}/{@code @Setter} de propósito: a entidade é praticamente
 * imutável após criada e {@code equals}/{@code hashCode} automáticos sobre todos os
 * campos seriam um anti-pattern com proxies do Hibernate.
 */
@Entity
@Table(name = "movie")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** {@code year} é palavra reservada em SQL, então a coluna recebe nome explícito. */
    @Column(name = "release_year", nullable = false)
    private int year;

    @Column(nullable = false)
    private String title;

    @Column
    private String studios;

    /** String crua de produtores como vem no CSV, ex.: {@code "A, B and C"}. */
    @Column(name = "producers", nullable = false, length = 1000)
    private String producers;

    @Column(nullable = false)
    private boolean winner;

    public Movie(int year, String title, String studios, String producers, boolean winner) {
        this.year = year;
        this.title = title;
        this.studios = studios;
        this.producers = producers;
        this.winner = winner;
    }
}
