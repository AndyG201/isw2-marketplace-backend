package co.edu.unbosque.marketplace;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ArquitecturaModularTest {

    @Test
    void verificarArquitecturaModular() {

        ApplicationModules.of(MarketplaceApplication.class)
                .verify();
    }
}
