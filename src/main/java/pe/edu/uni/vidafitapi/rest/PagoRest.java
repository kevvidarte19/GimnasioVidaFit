package pe.edu.uni.vidafitapi.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.uni.vidafitapi.dto.PagoDto;
import pe.edu.uni.vidafitapi.service.PagoService;

@RestController
@RequestMapping("api/pago")
public class PagoRest {
    @Autowired
    private PagoService pagoRequestService;

    @PostMapping("/suscripcion")
    public ResponseEntity<?> registrarPago(@RequestBody PagoDto dto) {
        try{
            PagoDto resultado = pagoRequestService.registrarPago(dto);
            return ResponseEntity.ok().body(resultado);
        }catch(RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }catch(Exception e){
            return ResponseEntity.internalServerError().body("Error inesperado: " + e.getMessage());
        }
    }
}
