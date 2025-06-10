package pe.edu.uni.vidafitapi.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder

public class RegistroAccesoDto {

    private int idSocio;
    private int idRegistradoPor; //ID del personal que registra
    private String fechaEntrada;

    //Campos adicionales para el reporte
    private String nombreSocio;
    private String estadoMembresia;
    private String vigenciaMembresia;
    private String mensaje;
}
