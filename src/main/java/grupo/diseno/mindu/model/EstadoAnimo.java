package grupo.diseno.mindu.model;

public enum EstadoAnimo {

    MUY_MAL(1, "Muy mal"),
    MAL(2, "Mal"),
    NEUTRAL(3, "Neutral"),
    BIEN(4, "Bien"),
    MUY_BIEN(5, "Muy bien");

    private final int valor;
    private final String etiqueta;

    EstadoAnimo(int valor, String etiqueta) {
        this.valor = valor;
        this.etiqueta = etiqueta;
    }

    public int getValor() {
        return valor;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}