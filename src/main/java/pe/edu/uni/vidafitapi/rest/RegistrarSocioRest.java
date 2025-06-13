package pe.edu.uni.vidafitapi.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.vidafitapi.dto.RegistrarSocioDto;
import pe.edu.uni.vidafitapi.service.RegistrarSocioService;

@RestController
@RequestMapping("/api/socios")
public class RegistrarSocioRest {

    @Autowired
    private RegistrarSocioService registrarSocioService;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarSocio(@RequestBody RegistrarSocioDto dto) {
        try {
            RegistrarSocioDto result = registrarSocioService.registrarSocio(dto);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/validar/dni/{dni}")
    public ResponseEntity<String> validarDni(@PathVariable String dni) {
        try {
            registrarSocioService.validarDniUnico(dni);
            return ResponseEntity.ok("El DNI " + dni + " está disponible.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/membresia/{id}")
    public ResponseEntity<String> validarMembresia(@PathVariable int id) {
        try {
            registrarSocioService.validarMembresia(id);
            return ResponseEntity.ok("La membresía con ID " + id + " es válida y está activa.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/personal/{id}")
    public ResponseEntity<String> validarPersonal(@PathVariable int id) {
        try {
            registrarSocioService.validarPersonal(id);
            return ResponseEntity.ok("El personal con ID " + id + " existe.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/socio-id/{id}")
    public ResponseEntity<String> validarIdSocioDisponible(@PathVariable int id) {
        try {
            registrarSocioService.validarIdSocioDisponible(id);
            return ResponseEntity.ok("El ID de socio " + id + " está disponible.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
