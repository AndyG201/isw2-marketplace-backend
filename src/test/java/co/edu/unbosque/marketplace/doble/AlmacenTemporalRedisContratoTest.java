package co.edu.unbosque.marketplace.doble;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import co.edu.unbosque.marketplace.usuario.application.port.AlmacenTemporal;

/** El mismo contrato contra la implementación real, con Redis. */
@SpringBootTest
@ActiveProfiles("test")
class AlmacenTemporalRedisContratoTest extends ContratoAlmacenTemporalTest {

    @Autowired
    private AlmacenTemporal almacen;

    @Override
    AlmacenTemporal almacen() {
        return almacen;
    }
}
