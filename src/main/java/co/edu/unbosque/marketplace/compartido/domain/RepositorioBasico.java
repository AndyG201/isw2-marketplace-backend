package co.edu.unbosque.marketplace.compartido.domain;

import java.util.List;
import java.util.Optional;

/**
 * Operaciones básicas de persistencia que comparten los repositorios del proyecto. Cada módulo declara su
 * propio puerto extendiendo esta interfaz; la implementación (Spring Data) vive en infraestructura.
 *
 * @param <E>  tipo de la entidad
 * @param <ID> tipo del identificador
 */
public interface RepositorioBasico<E, ID> {

    <S extends E> S save(S entidad);

    <S extends E> List<S> saveAll(Iterable<S> entidades);

    <S extends E> S saveAndFlush(S entidad);

    Optional<E> findById(ID id);

    List<E> findAll();

    boolean existsById(ID id);

    long count();

    void delete(E entidad);

    void deleteById(ID id);

    void flush();
}
