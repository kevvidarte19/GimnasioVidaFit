package pe.edu.uni.vidafitapi.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.vidafitapi.dto.CambioMembresiaDto;
import pe.edu.uni.vidafitapi.service.CambioMembresiaService;

@RestController
@RequestMapping("/api/procesos")
public class CambioMembresiaRest {

    @Autowired
    private CambioMembresiaService cambioMembresiaService;

    @PostMapping("/membresia")
    public ResponseEntity<?> cambiarMembresia(@RequestBody CambioMembresiaDto bean) {
        try {
            CambioMembresiaDto result = cambioMembresiaService.cambiarMembresia(bean);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    @GetMapping("/validar/socio-existe/{id}")
    public ResponseEntity<String> validarSocioExiste(@PathVariable int id) {
        try {
            cambioMembresiaService.validarSocioExiste(id);
            return ResponseEntity.ok("El socio con ID " + id + " existe.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/socio-activo/{id}")
    public ResponseEntity<String> validarSocioEstaActivo(@PathVariable int id) {
        try {
            cambioMembresiaService.validarSocioEstaActivo(id);
            return ResponseEntity.ok("El socio con ID " + id + " está activo.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/personal-existe/{id}")
    public ResponseEntity<String> validarPersonalExiste(@PathVariable int id) {
        try {
            cambioMembresiaService.validarPersonalExiste(id);
            return ResponseEntity.ok("El personal con ID " + id + " existe.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/membresia-existe/{id}")
    public ResponseEntity<String> validarNuevaMembresiaExiste(@PathVariable int id) {
        try {
            cambioMembresiaService.validarNuevaMembresiaExiste(id);
            return ResponseEntity.ok("La membresía con ID " + id + " existe.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/membresia-disponible/{id}")
    public ResponseEntity<String> validarNuevaMembresiaDisponible(@PathVariable int id) {
        try {
            cambioMembresiaService.validarNuevaMembresiaDisponible(id);
            return ResponseEntity.ok("La membresía con ID " + id + " está activa.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/no-misma-membresia/socio/{idSocio}/membresia/{idMembresia}")
    public ResponseEntity<String> validarNoEsLaMismaMembresia(@PathVariable int idSocio, @PathVariable int idMembresia) {
        try {
            CambioMembresiaDto dto = new CambioMembresiaDto();
            dto.setIdSocio(idSocio);
            dto.setIdNuevaMembresia(idMembresia);
            cambioMembresiaService.validarNoEsLaMismaMembresia(dto);
            return ResponseEntity.ok("El socio no tiene actualmente esta membresía activa, el cambio es válido.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}


