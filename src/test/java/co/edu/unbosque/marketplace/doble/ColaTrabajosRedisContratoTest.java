package co.edu.unbosque.marketplace.doble;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import co.edu.unbosque.marketplace.trabajo.ColaTrabajos;

/** El mismo contrato contra la implementación real, con Redis. */
@SpringBootTest
@ActiveProfiles("test")
class ColaTrabajosRedisContratoTest extends ContratoColaTrabajosTest {

    @Autowired
    private ColaTrabajos cola;

    @Override
    ColaTrabajos cola() {
        return cola;
    }
}
