package pe.edu.uni.vidafitapi.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class InscribirDto {

    private int idSocio;
    private int idClase;
    private String fecha;

    private int capacidadi;
    private int capacidadf;
}
