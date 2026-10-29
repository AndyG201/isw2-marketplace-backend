
package co.edu.unbosque.marketplace.usuario.api.response;

public class AutenticacionResponse {

    private String token;
    private String tipo = "Bearer";

    public AutenticacionResponse() {
    }

    public AutenticacionResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }
}
