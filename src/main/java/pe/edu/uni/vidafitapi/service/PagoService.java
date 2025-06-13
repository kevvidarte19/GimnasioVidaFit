package pe.edu.uni.vidafitapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.vidafitapi.dto.PagoDto;

@Service
public class PagoService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
    public PagoDto registrarPago(PagoDto dto) {
        //1.Validar que la suscripción exista y esté activa.
        validarSuscripcionExisteYActiva(dto.getIdSuscripcion());

        //2.Obtener el idSocio asociado a la suscripción para validaciones adicionales.
        Integer idSocio = obtenerIdSocioPorSuscripcion(dto.getIdSuscripcion());
        if (idSocio == null) {
            throw new RuntimeException("ERROR: No se pudo obtener el socio asociado a la suscripción.");
        }

        //3.Validaciones de existencia de entidades.
        validarSocioActivo(idSocio); //Se valida que el socio esté activo
        validarMetodoPagoExiste(dto.getIdMetodoPago());
        validarPersonalExiste(dto.getIdPersonal());

        //4.NUEVA VALIDACIÓN: Comprobar si la IDSuscripcion ya tiene un pago registrado.
        validarPagoExistenteParaSuscripcion(dto.getIdSuscripcion());

        //5.Validación del monto:¿El pago es por el monto correcto?
        validarMontoPago(dto.getIdSuscripcion(), dto.getMonto());

        //6.Obtener la duración en meses de la membresía para actualizar la fecha de fin.
        String sqlDuracion = "SELECT m.duracionMeses FROM Suscripcion s " +
                            "INNER JOIN Membresia m ON s.IDMembresia = m.IDMembresia " +
                             "WHERE s.IDSuscripcion = ?";
        Integer duracionMeses = jdbcTemplate.queryForObject(sqlDuracion, Integer.class, dto.getIdSuscripcion());

        //7.Insertar el registro del pago
        String sqlInsertPago = "INSERT INTO Pago (IDSuscripcion, IDMetodoPago, IDProcesadoPor, fechaPago, monto) " +
                               "VALUES (?, ?, ?, GETDATE(), ?)";
        jdbcTemplate.update(sqlInsertPago, dto.getIdSuscripcion(), dto.getIdMetodoPago(), dto.getIdPersonal(), dto.getMonto());

        //8.Actualizar las fechas de la suscripción para reflejar el inicio de la vigencia.
        String sqlUpdateSuscripcion = "UPDATE Suscripcion SET fechaInicio = GETDATE(), " +
                                      "fechaFin = DATEADD(MONTH, ?, GETDATE()) " +
                                      "WHERE IDSuscripcion = ?";
        jdbcTemplate.update(sqlUpdateSuscripcion, duracionMeses, dto.getIdSuscripcion());
        dto.setMensaje("Pago registrado y suscripción pagado exitosamente.");
        return dto;
    }
    //MÉTODOS DE VALIDACIÓN Y OBTENCIÓN DE DATOS
    //Validacion de Pago
    public void validarPagoExistenteParaSuscripcion(int idSuscripcion) {
        String sql = "SELECT COUNT(1) cont FROM Pago WHERE IDSuscripcion = ?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idSuscripcion);
        if (cont > 0) {
            throw new RuntimeException("ERROR: Esta suscripción ya tiene un pago registrado. No se puede realizar un nuevo pago hasta que termine la vigencia de la suscripción.");
        }
    }

    private Integer obtenerIdSocioPorSuscripcion(int idSuscripcion) {
        String sql = "SELECT IDSocio FROM Suscripcion WHERE IDSuscripcion = ?";
        try {
            return jdbcTemplate.queryForObject(sql, Integer.class, idSuscripcion);
        } catch (EmptyResultDataAccessException e) {
            return null; // No debería ocurrir si ya se validó la existencia de la suscripción
        }
    }

    public void validarSuscripcionExisteYActiva(int idSuscripcion) {
        String sql = "SELECT COUNT(1) cont FROM Suscripcion WHERE IDSuscripcion = ? AND activa = 1";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idSuscripcion);
        if ( cont == 0) {
            throw new RuntimeException("ERROR: La suscripción no existe o no está activa para procesar el pago.");
        }
    }

    public void validarMontoPago(int idSuscripcion, double montoRecibido) {
        String sql = "SELECT m.precio FROM Suscripcion s " +
                "JOIN Membresia m ON s.IDMembresia = m.IDMembresia " +
                "WHERE s.IDSuscripcion = ? AND s.activa = 1";
        double precioCorrecto;
        try {
            precioCorrecto = jdbcTemplate.queryForObject(sql, Double.class, idSuscripcion);
        } catch (EmptyResultDataAccessException e) {
            throw new RuntimeException("ERROR: No se pudo determinar el precio de la membresía para validar el monto de la suscripción.");
        }

        if (montoRecibido < precioCorrecto) {
            String diferencia = String.format("%.2f", precioCorrecto - montoRecibido);
            throw new RuntimeException("ERROR: Monto insuficiente.El pago debe ser de S/ "+ String.format("%.2f", precioCorrecto)+ ".Falta pagar S/ " + diferencia);
        }
        if (montoRecibido > precioCorrecto) {
            throw new RuntimeException("ERROR: El monto excede el precio de la membresía. El pago debe ser de S/ " + String.format("%.2f", precioCorrecto));
        }
    }

    private void validarSocioActivo(int idSocio) {
        String sql = "SELECT COUNT(1) cont FROM Socio s " +
                "JOIN EstadoSocio es ON s.IDEstadoSocio = es.IDEstadoSocio " +
                "WHERE s.IDSocio = ? AND es.descripcion = 'Activo'";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idSocio);
        if ( cont == 0) {
            throw new RuntimeException("ERROR: El socio asociado a esta suscripción no está activo.");
        }
    }

    public void validarMetodoPagoExiste(int idMetodo) {
        String sql = "SELECT COUNT(1) cont FROM MetodoPago WHERE IDMetodoPago = ?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idMetodo);
        if ( cont == 0) {
            throw new RuntimeException("ERROR: El método de pago no existe.");
        }
    }

    public void validarPersonalExiste(int idPersonal) {
        String sql = "SELECT COUNT(1) cont FROM Personal WHERE IDPersonal = ?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idPersonal);
        if ( cont == 0) {
            throw new RuntimeException("ERROR: El personal no existe.");
        }
    }
}



