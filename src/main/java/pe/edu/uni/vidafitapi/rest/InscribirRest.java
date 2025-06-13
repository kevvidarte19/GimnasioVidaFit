package pe.edu.uni.vidafitapi.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.uni.vidafitapi.dto.InscribirDto;
import pe.edu.uni.vidafitapi.service.InscribirService;

@RestController
@RequestMapping("/api/consultar")
public class InscribirRest {

    @Autowired
    private InscribirService inscribirService;

    @PostMapping("/inscribir")
    public ResponseEntity<?> inscribir(@RequestBody InscribirDto bean){
        try {
            InscribirDto resultado = inscribirService.inscribir(bean);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            e.printStackTrace(); // <-- muestra en consola la excepción
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e){
            e.printStackTrace(); // <-- muestra en consola errores más graves
            return ResponseEntity.internalServerError().body("Error interno al procesar la matricula.");
        }
    }

    @GetMapping("/validar/socio/{idSocio}")
    public ResponseEntity<String> validarSocio(@PathVariable int idSocio) {
        try {
            inscribirService.ValidarSocio(idSocio);
            return ResponseEntity.ok("El socio con ID " + idSocio + " existe.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/socio-activo/{idSocio}")
    public ResponseEntity<String> validarSocioActivo(@PathVariable int idSocio) {
        try {
            inscribirService.validarSocioActivo(idSocio);
            return ResponseEntity.ok("El socio con ID " + idSocio + " está activo.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/clase/{idClase}")
    public ResponseEntity<String> validarClase(@PathVariable int idClase) {
        try {
            inscribirService.ValidarClase(idClase);
            return ResponseEntity.ok("La clase con ID " + idClase + " existe.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/capacidad-clase/{idClase}")
    public ResponseEntity<String> validarCapacidadClase(@PathVariable int idClase) {
        try {
            // Para validar la capacidad, creamos un DTO temporal solo con el ID necesario.
            InscribirDto dto = InscribirDto.builder().idClase(idClase).build();
            inscribirService.ValidarCapacidad(dto);
            return ResponseEntity.ok("La clase con ID " + idClase + " tiene vacantes disponibles.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
