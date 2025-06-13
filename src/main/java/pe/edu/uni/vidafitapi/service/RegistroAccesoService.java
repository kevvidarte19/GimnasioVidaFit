package pe.edu.uni.vidafitapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.vidafitapi.dto.RegistroAccesoDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class RegistroAccesoService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public RegistroAccesoDto registrarAcceso(RegistroAccesoDto dto) {
        //Validaciones
        validarSocio(dto.getIdSocio());
        validarPersonal(dto.getIdRegistradoPor());
        validarMembresiaActiva(dto.getIdSocio());
        validarAccesoDelDia(dto.getIdSocio());

        //Insertar registro de acceso
        String sql = "INSERT INTO RegistroAcceso(IDSocio, IDRegistradoPor, fechaEntrada) VALUES(?, ?, GETDATE())";
        jdbcTemplate.update(sql, dto.getIdSocio(), dto.getIdRegistradoPor());

        // Obtener datos del socio para el reporte
        sql = """
            SELECT s.nombre + ' ' + s.apellido as nombreCompleto,
                   em.descripcion as estadoMembresia,
                   su.fechaFin
            FROM Socio s
            INNER JOIN Suscripcion su ON s.IDSocio = su.IDSocio
            INNER JOIN Membresia m ON su.IDMembresia = m.IDMembresia
            INNER JOIN EstadoMembresia em ON m.IDEstadoMembresia = em.IDEstadoMembresia
            WHERE s.IDSocio = ? AND su.activa = 1
            """;
        Map<String, Object> socioData = jdbcTemplate.queryForMap(sql, dto.getIdSocio());

        dto.setFechaEntrada(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        dto.setNombreSocio(socioData.get("nombreCompleto").toString());
        dto.setEstadoMembresia(socioData.get("estadoMembresia").toString());
        dto.setVigenciaMembresia(socioData.get("fechaFin").toString());
        dto.setMensaje("Se registró el acceso diario del socio con ID: " + dto.getIdSocio());

        return dto;
    }

    public List<Map<String, Object>> obtenerAccesosDiarios(String fecha) {
        String sql = """
            SELECT ra.IDSocio,
                   s.nombre + ' ' + s.apellido as nombreSocio,
                   ra.fechaEntrada,
                   p.nombre + ' ' + p.apellido as registradoPor,
                   m.nombre as tipoMembresia
            FROM RegistroAcceso ra
            INNER JOIN Socio s ON ra.IDSocio = s.IDSocio
            INNER JOIN Personal p ON ra.IDRegistradoPor = p.IDPersonal
            INNER JOIN Suscripcion su ON s.IDSocio = su.IDSocio AND su.activa = 1
            INNER JOIN Membresia m ON su.IDMembresia = m.IDMembresia
            WHERE CAST(ra.fechaEntrada AS DATE) = ?
            ORDER BY ra.fechaEntrada DESC
            """;
        return jdbcTemplate.queryForList(sql, fecha);
    }

    public Map<String, Object> obtenerEstadisticasAcceso(String fecha) {
        String sql = """
            SELECT
                COUNT(*) as totalAccesos,
                COUNT(DISTINCT IDSocio) as sociosUnicos,
                MIN(fechaEntrada) as primerAcceso,
                MAX(fechaEntrada) as ultimoAcceso
            FROM RegistroAcceso
            WHERE CAST(fechaEntrada AS DATE) = ?
            """;
        return jdbcTemplate.queryForMap(sql, fecha);
    }

    public List<Map<String, Object>> obtenerAccesosSocioHoy(int idSocio) {
        String fechaHoy = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String sql = """
            SELECT ra.fechaEntrada, p.nombre + ' ' + p.apellido as registradoPor
            FROM RegistroAcceso ra
            INNER JOIN Personal p ON ra.IDRegistradoPor = p.IDPersonal
            WHERE ra.IDSocio = ? AND CAST(ra.fechaEntrada AS DATE) = ?
            ORDER BY ra.fechaEntrada DESC
            """;
        return jdbcTemplate.queryForList(sql, idSocio, fechaHoy);
    }

    //Validaciones
    public void validarSocio(int idSocio) {
        String sql = "SELECT COUNT(1) cont FROM Socio WHERE IDSocio = ?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idSocio);
        if (cont == 0) {
            throw new RuntimeException("ERROR: El socio no existe.");
        }

        //Validar que el socio esté activo
        sql = """
            SELECT COUNT(1) cont FROM Socio s
            INNER JOIN EstadoSocio es ON s.IDEstadoSocio = es.IDEstadoSocio
            WHERE s.IDSocio = ? AND es.descripcion = 'ACTIVO'
            """;
        cont = jdbcTemplate.queryForObject(sql, Integer.class, idSocio);
        if (cont == 0) {
            throw new RuntimeException("ERROR: El socio no está activo.");
        }
    }

    public void validarPersonal(int idPersonal) {
        String sql = "SELECT COUNT(1) cont FROM Personal WHERE IDPersonal = ?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idPersonal);
        if (cont == 0) {
            throw new RuntimeException("ERROR: El personal no existe.");
        }
    }

    public void validarMembresiaActiva(int idSocio) {
        String sql = """
            SELECT COUNT(1) cont FROM Suscripcion su
            INNER JOIN Membresia m ON su.IDMembresia = m.IDMembresia
            INNER JOIN EstadoMembresia em ON m.IDEstadoMembresia = em.IDEstadoMembresia
            WHERE su.IDSocio = ? 
            AND su.fechaFin >= GETDATE()
            AND su.activa = 1
            """;

        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idSocio);
        if (cont == 0) {
            throw new RuntimeException("ERROR: El socio no tiene una membresía activa o está vencida.");
        }
    }

    private void validarAccesoDelDia(int idSocio) {
        //Validar que el socio no haya registrado acceso hoy
        String sql = """
            SELECT COUNT(1) cont FROM RegistroAcceso 
            WHERE IDSocio = ? AND CAST(fechaEntrada AS DATE) = CAST(GETDATE() AS DATE)
            """;
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idSocio);
        if (cont > 0) {
            throw new RuntimeException("ERROR: El socio ya registró acceso el día de hoy.");
        }
    }
}
