package pe.edu.uni.vidafitapi.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/validar/suscripcion/{id}")
    public ResponseEntity<String> validarSuscripcion(@PathVariable int id) {
        try {
            pagoRequestService.validarSuscripcionExisteYActiva(id);
            return ResponseEntity.ok("La suscripción con ID " + id + " es válida y está activa.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/personal/{id}")
    public ResponseEntity<String> validarPersonal(@PathVariable int id) {
        try {
            pagoRequestService.validarPersonalExiste(id);
            return ResponseEntity.ok("El personal con ID " + id + " existe.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/metodopago/{id}")
    public ResponseEntity<String> validarMetodoPago(@PathVariable int id) {
        try {
            pagoRequestService.validarMetodoPagoExiste(id);
            return ResponseEntity.ok("El método de pago con ID " + id + " es válido.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/monto/{idSuscripcion}/{monto}")
    public ResponseEntity<String> validarMonto(
            @PathVariable int idSuscripcion,
            @PathVariable double monto) {
        try {
            pagoRequestService.validarMontoPago(idSuscripcion, monto);
            return ResponseEntity.ok("El monto de S/ " + String.format("%.2f", monto) + " es correcto para la suscripción " + idSuscripcion + ".");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/validar/pago-existente/{idSuscripcion}")
    public ResponseEntity<String> validarPagoExistente(@PathVariable int idSuscripcion) {
        try {
            pagoRequestService.validarPagoExistenteParaSuscripcion(idSuscripcion);
            return ResponseEntity.ok("La suscripción con ID " + idSuscripcion + " no tiene un pago vigente y puede ser procesada.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}

