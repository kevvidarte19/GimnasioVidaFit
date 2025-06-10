package pe.edu.uni.vidafitapi.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class PagoDto {
    private int idSuscripcion;
    private int idMetodoPago;
    private int idPersonal;
    private double monto;
    private String mensaje;
}
