package co.edu.unbosque.marketplace.resena.application.port;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.unbosque.marketplace.resena.domain.Resena;

@Repository
public interface ResenaModeracionRepository extends JpaRepository<Resena, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Resena r where r.idResena = :idResena")
    Optional<Resena> bloquearPorId(@Param("idResena") UUID idResena);
}
