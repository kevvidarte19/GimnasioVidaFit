package pe.edu.uni.vidafitapi.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

}
