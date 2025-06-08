package pe.edu.uni.vidafitapi.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
}
