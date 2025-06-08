package pe.edu.uni.vidafitapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.vidafitapi.dto.RegistrarSocioDto;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;

@Service
public class RegistrarSocioService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional
    public Long registrarSocio(RegistrarSocioDto dto) {
        //1.Validar datos básicos
        if (!validarDatosCompletos(dto)) {
            throw new IllegalArgumentException("Datos incompletos. Verifique nombre, apellido, DNI, correo, teléfono, ID de membresía y personal");
        }

        //2.Validar que el personal existe
        if (!existePersonal(dto.getIdRegistradoPor())) {
            throw new IllegalArgumentException("El personal que registra no existe");
        }

        //3.Validar que el DNI no esté registrado
        if (existeSocioPorDni(dto.getDni())) {
            throw new IllegalArgumentException("Ya existe un socio registrado con este DNI: " + dto.getDni());
        }

        //4.Validar que la membresía existe
        if (!existeMembresia(dto.getIdMembresia())) {
            throw new IllegalArgumentException("La membresía seleccionada no existe");
        }

        //5.Validar que la membresía está activa
        if (!membresiaActiva(dto.getIdMembresia())) {
            throw new IllegalArgumentException("La membresía seleccionada no está activa");
        }

        // 6. Validar duración de la membresía
        Integer duracionMeses = obtenerDuracionMembresia(dto.getIdMembresia());
        if (duracionMeses == null || duracionMeses <= 0) {
            throw new IllegalArgumentException("La duración de la membresía no es válida");
        }

        //7.Registrar socio
        Long idSocio = insertarSocio(dto);

        //8.Calcular fechas de suscripción
        LocalDate fechaInicio = dto.getFechaInicio() != null ? dto.getFechaInicio() : LocalDate.now();
        LocalDate fechaFin = dto.getFechaFin() != null ? dto.getFechaFin() : fechaInicio.plusMonths(duracionMeses);

        //9.Crear suscripción
        crearSuscripcion(idSocio, dto.getIdMembresia(), dto.getIdRegistradoPor(), fechaInicio, fechaFin);

        return idSocio;
    }

    //Métodos de validación ---
    private boolean validarDatosCompletos(RegistrarSocioDto dto) {
        return dto.getNombre() != null && !dto.getNombre().isBlank() &&
                dto.getApellido() != null && !dto.getApellido().isBlank() &&
                dto.getDni() != null && !dto.getDni().isBlank() &&
                dto.getCorreo() != null && !dto.getCorreo().isBlank() &&
                dto.getTelefono() != null && !dto.getTelefono().isBlank() &&
                dto.getIdMembresia() != null &&
                dto.getIdRegistradoPor() != null;
    }

    private boolean existePersonal(Long idPersonal) {
        String sql = "SELECT COUNT(*) FROM Personal WHERE IDPersonal = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idPersonal);
        return count != null && count > 0;
    }

    private boolean existeSocioPorDni(String dni) {
        String sql = "SELECT COUNT(*) FROM Socio WHERE dni = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, dni);
        return count != null && count > 0;
    }

    private boolean existeMembresia(Long idMembresia) {
        String sql = "SELECT COUNT(*) FROM Membresia WHERE IDMembresia = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idMembresia);
        return count != null && count > 0;
    }

    private boolean membresiaActiva(Long idMembresia) {
        String sql = """
            SELECT COUNT(*) FROM Membresia m
            INNER JOIN EstadoMembresia em ON m.IDEstadoMembresia = em.IDEstadoMembresia
            WHERE m.IDMembresia = ? AND em.descripcion = 'Activa'
            """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, idMembresia);
        return count != null && count > 0;
    }

    private Integer obtenerDuracionMembresia(Long idMembresia) {
        String sql = "SELECT duracionMeses FROM Membresia WHERE IDMembresia = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, idMembresia);
        } catch (Exception e) {
            return null;
        }
    }

    //Métodos de operaciones en BD
    private Long insertarSocio(RegistrarSocioDto dto) {
        String sql = """
            INSERT INTO Socio (IDEstadoSocio, nombre, apellido, dni, correo, telefono, fechaAlta)
            VALUES (1, ?, ?, ?, ?, ?, ?)  -- 1 = Estado 'Activo'
            """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(con -> {
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, dto.getNombre());
            ps.setString(2, dto.getApellido());
            ps.setString(3, dto.getDni());
            ps.setString(4, dto.getCorreo());
            ps.setString(5, dto.getTelefono());
            ps.setDate(6, java.sql.Date.valueOf(LocalDate.now()));
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    private void crearSuscripcion(Long idSocio, Long idMembresia,
                                  Long idRegistradoPor, LocalDate inicio, LocalDate fin) {
        String sql = """
            INSERT INTO Suscripcion (IDSocio, IDMembresia, IDRegistradoPor, 
                                   fechaInicio, fechaFin, activa, fechaRegistro)
            VALUES (?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP)
            """;

        jdbcTemplate.update(sql,
                idSocio,
                idMembresia,
                idRegistradoPor,
                java.sql.Date.valueOf(inicio),
                java.sql.Date.valueOf(fin));
    }
}
