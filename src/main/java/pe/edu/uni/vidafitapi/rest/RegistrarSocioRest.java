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
}
