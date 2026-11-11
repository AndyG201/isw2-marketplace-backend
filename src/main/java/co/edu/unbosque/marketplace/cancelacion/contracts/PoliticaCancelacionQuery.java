
package co.edu.unbosque.marketplace.cancelacion.contracts;

import java.util.UUID;

public interface PoliticaCancelacionQuery {

    boolean existePoliticaActiva(UUID idPolitica);
}
