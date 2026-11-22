package co.edu.unbosque.marketplace.doble;


import co.edu.unbosque.marketplace.trabajo.ColaTrabajos;

class ColaTrabajosEnMemoriaContratoTest extends ContratoColaTrabajosTest {

    private final ColaTrabajos cola = new ColaTrabajosEnMemoria();

    @Override
    ColaTrabajos cola() {
        return cola;
    }
}
