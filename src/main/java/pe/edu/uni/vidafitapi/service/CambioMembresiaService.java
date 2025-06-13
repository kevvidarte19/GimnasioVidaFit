package pe.edu.uni.vidafitapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.vidafitapi.dto.CambioMembresiaDto;

@Service
public class CambioMembresiaService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public CambioMembresiaDto cambiarMembresia(CambioMembresiaDto dto) {
        //Variable
        String sql;

        //Validaciones
        validarSocioExiste(dto.getIdSocio());
        validarNuevaMembresiaExiste(dto.getIdNuevaMembresia());
        validarNoEsLaMismaMembresia(dto);
        validarSocioEstaActivo(dto.getIdSocio());
        validarNuevaMembresiaDisponible(dto.getIdNuevaMembresia());
        validarPersonalExiste(dto.getIdPersonal());

        //Obtener informacion para el reporte
        //Obtener precio y tipo de membresía anterior
        sql = """
                SELECT m.Precio, m.nombre 
                FROM Suscripcion s 
                INNER JOIN Membresia m ON s.IDMembresia = m.IDMembresia
                WHERE s.IDSocio = ? AND s.activa = 1
                """;
        Object[] infoAnterior = jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> new Object[]{rs.getDouble("Precio"), rs.getString("nombre")},
                dto.getIdSocio());

        //Obtener precio y tipo de nueva membresía
        sql = "SELECT Precio, nombre FROM Membresia WHERE IDMembresia = ?";
        Object[] infoNueva = jdbcTemplate.queryForObject(sql,
                (rs, rowNum) -> new Object[]{rs.getDouble("Precio"), rs.getString("nombre")},
                dto.getIdNuevaMembresia());

        //Obtener duración de nueva membresía
        sql = "SELECT duracionMeses FROM Membresia WHERE IDMembresia = ?";
        int duracionMeses = jdbcTemplate.queryForObject(sql, Integer.class, dto.getIdNuevaMembresia());


        //Proceso
        //Paso 1:Desactivar suscripción actual
        sql = """
                UPDATE Suscripcion 
                SET activa = 0, 
                    FechaFin = GETDATE()
                WHERE IDSocio = ? AND activa = 1
                """;
        jdbcTemplate.update(sql, dto.getIdSocio());

        //Paso 2:Crear nueva suscripción
        sql = """
                INSERT INTO Suscripcion(IDSocio, IDMembresia, fechaInicio, fechaFin,activa, fechaRegistro, IDRegistradoPor)
                VALUES(?, ?, GETDATE(), DATEADD(month, ?, GETDATE()), 1, GETDATE(), ?)
                """;
        jdbcTemplate.update(sql, dto.getIdSocio(), dto.getIdNuevaMembresia(), duracionMeses, dto.getIdPersonal());

        //Completamos el Dto para el reporte
        dto.setPrecioAnterior((Double) infoAnterior[0]);
        dto.setTipoMembresiaAnterior((String) infoAnterior[1]);
        dto.setPrecioNuevo((Double) infoNueva[0]);
        dto.setTipoMembresiaNueva((String) infoNueva[1]);
        dto.setMensaje("Cambio de membresía realizado exitosamente");

        return dto;
    }

    //Validamos de que el socio exista
    public void validarSocioExiste(int idSocio) {
        String sql = "SELECT COUNT(1) cont FROM Socio WHERE IDSocio = ?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idSocio);
        if (cont == 0) {
            throw new RuntimeException("ERROR: El socio no existe.");
        }
    }

    //Validamos que la nueva membresia exista
    public void validarNuevaMembresiaExiste(int idMembresia) {
        String sql = "SELECT COUNT(1) cont FROM Membresia WHERE IDMembresia = ?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idMembresia);
        if (cont == 0) {
            throw new RuntimeException("ERROR: La membresía no existe.");
        }
    }

    //Validamos que no sea la misma membresia cuando el socio elija
    public void validarNoEsLaMismaMembresia(CambioMembresiaDto dto) {
        String sql = """
                SELECT COUNT(1) cont FROM Suscripcion 
                WHERE IDSocio = ? AND IDMembresia = ? AND activa = 1
                """;
        int cont = jdbcTemplate.queryForObject(sql, Integer.class,
                dto.getIdSocio(), dto.getIdNuevaMembresia());
        if (cont > 0) {
            throw new RuntimeException("ERROR: Ya tienes esa membresía activa.");
        }
    }

    //Validamos de que si el socio exista también tiene que estar activo
    public void validarSocioEstaActivo(int idSocio) {
        String sql = """
                SELECT COUNT(1) cont FROM Socio s 
                INNER JOIN EstadoSocio es ON s.IDEstadoSocio = es.IDEstadoSocio
                WHERE s.IDSocio = ? AND es.descripcion = 'Activo'
                """;
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idSocio);
        if (cont == 0) {
            throw new RuntimeException("ERROR: El socio no está activo.");
        }
    }

    //Validamos que si la membresia exista tambien tiene que estar activa
    public void validarNuevaMembresiaDisponible(int idMembresia) {
        String sql = """
                SELECT COUNT(1) cont FROM Membresia m 
                INNER JOIN EstadoMembresia em ON m.IDEstadoMembresia = em.IDEstadoMembresia
                WHERE m.IDMembresia = ? AND em.descripcion = 'Activa'
                """;
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idMembresia);
        if (cont == 0) {
            throw new RuntimeException("ERROR: La membresía con ID 4 no está activa.");
        }
    }

    //Validamos que el personal exista para que registre la suscripcion
    public void validarPersonalExiste(int idPersonal) {
        String sql = "SELECT COUNT(1) cont FROM Personal WHERE IDPersonal = ?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idPersonal);
        if (cont == 0) {
            throw new RuntimeException("ERROR: El personal no existe.");
        }
    }
}
