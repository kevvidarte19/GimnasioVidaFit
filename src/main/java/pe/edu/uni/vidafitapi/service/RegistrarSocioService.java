package pe.edu.uni.vidafitapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.vidafitapi.dto.RegistrarSocioDto;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;

@Service
public class RegistrarSocioService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public RegistrarSocioDto registrarSocio(RegistrarSocioDto dto) {

        //Validaciones Generales
        validarDatosCompletos(dto);
        validarPersonal(dto.getIdRegistradoPor());
        validarDniUnico(dto.getDni());
        validarMembresia(dto.getIdMembresia());

        int idSocioFinal;
        //Lógica de Inserción
        if (dto.getIdSocio() != 0) {
            //El usuario proporcionó un ID,hay que validarlo e insertarlo.
            validarIdSocioDisponible(dto.getIdSocio());
            idSocioFinal = insertarSocioConId(dto);
        } else {
            //El usuario no proporcionó ID, se autogenera.
            idSocioFinal = insertarSocioSinId(dto);
        }

        //Crear Suscripción (usa el ID final)
        LocalDate fechaInicio = (dto.getFechaInicio() != null) ? dto.getFechaInicio() : LocalDate.now();
        LocalDate fechaFin = (dto.getFechaFin() != null) ? dto.getFechaFin() : calcularFechaFin(dto.getIdMembresia());

        String sqlSuscripcion = """
            INSERT INTO Suscripcion (IDSocio, IDMembresia, IDRegistradoPor, fechaInicio, fechaFin, activa, fechaRegistro)
            VALUES (?, ?, ?, ?, ?, 1, GETDATE())
            """;

        jdbcTemplate.update(sqlSuscripcion, idSocioFinal, dto.getIdMembresia(),
                dto.getIdRegistradoPor(), fechaInicio, fechaFin);

        //Completar DTO para la respuesta
        dto.setIdSocio(idSocioFinal);
        dto.setMensaje("Socio con ID " + idSocioFinal + " ha sido registrado exitosamente.");
        return dto;
    }

    //MÉTODOS PRIVADOS DE LÓGICA Y VALIDACIÓN
    private void validarIdSocioDisponible(int idSocio) {
        String sql = "SELECT COUNT(1) FROM Socio WHERE IDSocio = ?";
        int count = jdbcTemplate.queryForObject(sql, Integer.class, idSocio);
        if (count > 0) {
            throw new RuntimeException("ERROR: El ID de socio " + idSocio + " ya está en uso.");
        }
    }

    private int insertarSocioConId(RegistrarSocioDto dto) {
        jdbcTemplate.execute("SET IDENTITY_INSERT Socio ON");
        try {
            String sql = """
                INSERT INTO Socio (IDSocio, IDEstadoSocio, nombre, apellido, dni, correo, telefono, fechaAlta)
                VALUES (?, 1, ?, ?, ?, ?, ?, GETDATE())
                """;
            jdbcTemplate.update(sql, dto.getIdSocio(), dto.getNombre(),
                    dto.getApellido(), dto.getDni(), dto.getCorreo(), dto.getTelefono());
            return dto.getIdSocio();
        } finally {
            jdbcTemplate.execute("SET IDENTITY_INSERT Socio OFF");
        }
    }

    private int insertarSocioSinId(RegistrarSocioDto dto) {
        String sql = """
            INSERT INTO Socio (IDEstadoSocio, nombre, apellido, dni, correo, telefono, fechaAlta)
            VALUES (1, ?, ?, ?, ?, ?, GETDATE())
            """;
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, dto.getNombre());
            ps.setString(2, dto.getApellido());
            ps.setString(3, dto.getDni());
            ps.setString(4, dto.getCorreo());
            ps.setString(5, dto.getTelefono());
            return ps;
        }, keyHolder);

        if (keyHolder.getKey() == null) {
            throw new RuntimeException("Error: No se pudo obtener el ID autogenerado del socio.");
        }
        return keyHolder.getKey().intValue();
    }

    //Validaciones Generales
    private void validarDatosCompletos(RegistrarSocioDto dto) {
        if (dto.getNombre() == null || dto.getNombre().isEmpty() ||
                dto.getApellido() == null || dto.getApellido().isEmpty() ||
                dto.getDni() == null || dto.getDni().isEmpty() ||
                dto.getCorreo() == null || dto.getCorreo().isEmpty() ||
                dto.getTelefono() == null || dto.getTelefono().isEmpty() ||
                dto.getIdMembresia() == 0 ||
                dto.getIdRegistradoPor() == 0) {
            throw new RuntimeException("ERROR: Datos incompletos. Verifique todos los campos requeridos.");
        }
    }
    private void validarPersonal(int idPersonal) {
        String sql = "SELECT COUNT(1) cont FROM Personal WHERE IDPersonal = ?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idPersonal);
        if (cont == 0) {
            throw new RuntimeException("ERROR: El personal con ID " + idPersonal + " no existe.");
        }
    }
    private void validarDniUnico(String dni) {
        String sql = "SELECT COUNT(1) cont FROM Socio WHERE dni = ?";
        Integer cont = jdbcTemplate.queryForObject(sql, Integer.class, dni);
        if (cont != null && cont > 0) {
            throw new RuntimeException("ERROR: El DNI " + dni + " ya está registrado.");
        }
    }
    private void validarMembresia(int idMembresia) {
        String sql = "SELECT COUNT(1) cont FROM Membresia m " +
                "JOIN EstadoMembresia em ON m.IDEstadoMembresia = em.IDEstadoMembresia " +
                "WHERE m.IDMembresia = ? AND em.descripcion = 'Activa'";
        Integer cont = jdbcTemplate.queryForObject(sql, Integer.class, idMembresia);
        if (cont == null || cont == 0) {
            throw new RuntimeException("ERROR: La membresía no existe o esta inactiva.");
        }
    }
    private LocalDate calcularFechaFin(int idMembresia) {
        String sql = "SELECT duracionMeses FROM Membresia WHERE IDMembresia = ?";
        Integer duracionMeses = jdbcTemplate.queryForObject(sql, Integer.class, idMembresia);
        if (duracionMeses == null || duracionMeses <= 0) {
            throw new RuntimeException("Duración de la membresía inválida.");
        }
        return LocalDate.now().plusMonths(duracionMeses);
    }
}
