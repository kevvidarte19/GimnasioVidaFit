package pe.edu.uni.vidafitapi.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString

public class RegistrarSocioDto {

    //Datos del socio
    private String nombre;
    private String apellido;
    private String dni;
    private String correo;
    private String telefono;

    //Información de suscripción
    private Long idMembresia;  //ID de la membresía a asignar
    private Long idRegistradoPor; //ID del personal que registra

    //Fechas de la suscripción (pueden ser calculadas si no se envían)
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

}
