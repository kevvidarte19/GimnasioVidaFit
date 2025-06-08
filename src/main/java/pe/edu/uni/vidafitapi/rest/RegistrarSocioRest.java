package pe.edu.uni.vidafitapi.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.uni.vidafitapi.dto.RegistrarSocioDto;
import pe.edu.uni.vidafitapi.service.RegistrarSocioService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/socios")
public class RegistrarSocioRest {

    @Autowired
    private RegistrarSocioService socioService;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarSocio(@RequestBody RegistrarSocioDto dto) {
        try {
            Long idSocio = socioService.registrarSocio(dto);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("codigo", HttpStatus.CREATED.value());
            response.put("idSocio", idSocio);
            response.put("mensaje", "Socio registrado exitosamente");
            response.put("fechaRegistro", LocalDateTime.now());
            response.put("detalle", "El socio ha sido registrado con todos los datos requeridos");

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            // Manejo específico para errores de validación del negocio
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("codigo", HttpStatus.BAD_REQUEST.value());
            errorResponse.put("mensaje", e.getMessage());
            errorResponse.put("fechaError", LocalDateTime.now());

            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            // Manejo de errores inesperados
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("codigo", HttpStatus.INTERNAL_SERVER_ERROR.value());
            errorResponse.put("mensaje", "Error interno del servidor");
            errorResponse.put("detalle", e.getMessage());
            errorResponse.put("fechaError", LocalDateTime.now());

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
