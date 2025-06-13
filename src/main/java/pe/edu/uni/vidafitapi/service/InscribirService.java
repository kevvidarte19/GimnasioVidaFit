package pe.edu.uni.vidafitapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.uni.vidafitapi.dto.InscribirDto;

@Service
public class InscribirService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)

    public InscribirDto inscribir(InscribirDto bean){
        //variables
        String sql;
        int capacidad;

        //Validaciones
        ValidarSocio(bean.getIdSocio());
        validarSocioActivo(bean.getIdSocio());
        ValidarClase(bean.getIdClase());
        ValidarCapacidad(bean);
        //Procesos

        sql= """
                insert into InscripcionClase(IDSocio,IDClase,fechaInscripcion)
                values(?,?,GETDATE())
                """;
        Object [] datos={
                bean.getIdSocio(),bean.getIdClase()
        };
        jdbcTemplate.update(sql,datos);

        sql= """
                select CONVERT(varchar(20),fechaInscripcion,120) fecha 
                from InscripcionClase where IDSocio=? and IDClase=?;
                
                """;
        String fecha = jdbcTemplate.queryForObject(sql, String.class, bean.getIdSocio(), bean.getIdClase());

        sql= """
                update Clase
                set capacidad=capacidad-1 where IDClase=?
                """;
        jdbcTemplate.update(sql,bean.getIdClase());

        sql = "select capacidad from Clase where IDClase=?";
        capacidad=jdbcTemplate.queryForObject(sql, Integer.class, bean.getIdClase());

        //fin
        bean.setCapacidadf(capacidad);
        bean.setFecha(fecha);
        return bean;
    }

    public void ValidarCapacidad(InscribirDto bean) {
        String sql="select capacidad from Clase where IDClase=?";
        int capacidad = jdbcTemplate.queryForObject(sql, Integer.class, bean.getIdClase());
        if(capacidad==0){
            throw new RuntimeException("No hay vacantes disponibles para esta clase.");
        }else {
            bean.setCapacidadi(capacidad);
        }
    }

    public void ValidarClase(int idClase) {
        String sql="select count(1) cont from Clase where IDClase=?";
        int cont = jdbcTemplate.queryForObject(sql, Integer.class, idClase);
        if(cont==0){
            throw new RuntimeException("Clase" + idClase + " no existe");
        }
    }

    public void ValidarSocio(int idSocio) {
        String sql="select count(1) cont from Socio where IDSocio=?";
        int cont= jdbcTemplate.queryForObject(sql, Integer.class,idSocio);
        if(cont==0){
            throw new RuntimeException("Socio " + idSocio + " no existe.");

        }
    }

    public void validarSocioActivo(int idSocio) {
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
}
