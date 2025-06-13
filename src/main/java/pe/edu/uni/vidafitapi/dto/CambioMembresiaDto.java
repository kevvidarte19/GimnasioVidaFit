package pe.edu.uni.vidafitapi.dto;


import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder

public class CambioMembresiaDto {

    private int idSocio;
    private int idNuevaMembresia;
    private int idPersonal; // Personal que autoriza el cambio
    private double precioAnterior;
    private double precioNuevo;
    private String tipoMembresiaAnterior;
    private String tipoMembresiaNueva;
    private String mensaje;

}
