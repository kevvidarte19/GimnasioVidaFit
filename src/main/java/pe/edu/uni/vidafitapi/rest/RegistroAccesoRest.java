package pe.edu.uni.vidafitapi.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.vidafitapi.dto.RegistroAccesoDto;
import pe.edu.uni.vidafitapi.service.RegistroAccesoService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/acceso")
public class RegistroAccesoRest {

    @Autowired
    private RegistroAccesoService registroAccesoService;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarAcceso(@RequestBody RegistroAccesoDto dto) {
        try {
            RegistroAccesoDto result = registroAccesoService.registrarAcceso(dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/diario")
    public ResponseEntity<?> obtenerAccesosDiarios(@RequestParam(required = false) String fecha) {
        try {
            if (fecha == null || fecha.trim().isEmpty()) {
                fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            List<Map<String, Object>> accesos = registroAccesoService.obtenerAccesosDiarios(fecha);
            return ResponseEntity.ok(accesos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<?> obtenerEstadisticasAcceso(@RequestParam(required = false) String fecha) {
        try {
            if (fecha == null || fecha.trim().isEmpty()) {
                fecha = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            Map<String, Object> estadisticas = registroAccesoService.obtenerEstadisticasAcceso(fecha);
            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/socio/{idSocio}")
    public ResponseEntity<?> obtenerAccesosSocio(@PathVariable int idSocio) {
        try {
            List<Map<String, Object>> accesos = registroAccesoService.obtenerAccesosSocioHoy(idSocio);
            if (accesos.isEmpty()) {
                Map<String, String> response = new HashMap<>();
                response.put("mensaje", "El socio con ID " + idSocio + " no registró acceso el día de hoy.");
                return ResponseEntity.ok(response);
            }
            return ResponseEntity.ok(accesos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/socio/{idSocio}")
    public ResponseEntity<String> validarSocio(@PathVariable int idSocio) {
        try {
            registroAccesoService.validarSocio(idSocio);
            return ResponseEntity.ok("El socio con ID " + idSocio + " existe y está activo.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/personal/{idPersonal}")
    public ResponseEntity<String> validarPersonal(@PathVariable int idPersonal) {
        try {
            registroAccesoService.validarPersonal(idPersonal);
            return ResponseEntity.ok("El personal con ID " + idPersonal + " existe.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/membresia/{idSocio}")
    public ResponseEntity<String> validarMembresia(@PathVariable int idSocio) {
        try {
            registroAccesoService.validarMembresiaActiva(idSocio);
            return ResponseEntity.ok("El socio con ID " + idSocio + " tiene una membresía activa y vigente.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
