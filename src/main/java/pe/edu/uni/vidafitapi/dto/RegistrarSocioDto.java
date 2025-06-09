package pe.edu.uni.vidafitapi.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString

public class RegistrarSocioDto {

    // Datos del socio
    private int idSocio;
    private String nombre;
    private String apellido;
    private String dni;
    private String correo;
    private String telefono;

    // Información de suscripción
    private int idMembresia;
    private int idRegistradoPor;
    // Fechas (
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    // Mensaje de respuesta
    private String mensaje;
}
