package com.mutualidad.modulo_credito.Repository;

import com.mutualidad.modulo_credito.Models.ModelSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<ModelSolicitud, Integer> {

    @Procedure(name = "Solicitud.pa_InsertarSolicitudCredito")
    String pa_InsertarSolicitudCredito(
            @Param("asesor") String asesor,
            @Param("num_socio") Integer numSocio,
            @Param("tipo_credito") String tipoCredito,
            @Param("empresa") String empresa,
            @Param("monto") BigDecimal monto,
            @Param("plazo") Integer plazo,
            @Param("ahorros_alMomento") BigDecimal ahorrosAlMomento,
            @Param("tasa") BigDecimal tasa,
            @Param("mora") BigDecimal mora,
            @Param("iva") BigDecimal iva,
            @Param("riesgo") Integer riesgo,
            @Param("gradualidad") BigDecimal gradualidad,
            @Param("monto_riesgo") BigDecimal montoRiesgo,
            @Param("bonif_aplica") String bonifAplica,
            @Param("tipo_riesgo") String tipoRiesgo,
            @Param("datos_avales") String datosAvales,
            @Param("datos_propiedad") String datosPropiedad,
            @Param("direccion") String direccion,
            @Param("libre_gravamen") Boolean libreGravamen,
            @Param("acreditacion") String acreditacion,
            @Param("Resultado") String Resultado
    );

    @Procedure(name = "Solicitud.pa_CancelarSolicitudCredito")
    String pa_CancelarSolicitudCredito(
            @Param("id_solicitud") int id_solicitud,
            @Param("Resultado") String Resultado
    );

    @Procedure(name = "Solicitud.pa_ConfirmarSolicitud")
    String pa_ConfirmarSolicitud(
            @Param("id_credito") int id_solicitud,
            @Param("plazo") int plazo,
            @Param("tasa") Double tasa,
            @Param("iva") Double iva,
            @Param("mora") Double mora,
            @Param("is_desembolso") int isDesembolso,
            @Param("monto_credito") BigDecimal montoCredito ,
            @Param("dictamen") String dictamen ,
            @Param("Resultado") String Resultado
            );



    List<ModelSolicitud> findByEstado(int estado);


    @Query(value = "SELECT * FROM VW_SOLICITUD_CREDITO_DETALLE WHERE ID = :idSolicitud ", nativeQuery = true)
    List<Object[]> traerSolicitudPendiente(@Param("idSolicitud") int idSolicitud);

    @Query(value = "SELECT * FROM VW_AVAL_CREDITO WHERE ID_CREDITO = :idSolicitud ", nativeQuery = true)
    List<Object[]> traerAvalesSolicitudPendiente(@Param("idSolicitud") int idSolicitud);


    @Query(value = "SELECT * FROM VW_PROPIEDAD_CREDITO_DETALLE WHERE ID_CREDITO = :idSolicitud ", nativeQuery = true)
    List<Object[]> traerPropietariosSolicitudPendiente(@Param("idSolicitud") int idSolicitud);

    @Procedure(name = "Solicitud.pa_ModificarSolicitud")
    String pa_modificarSolicitud(
            @Param("id_credito") Integer idCredito,
            @Param("tipo_credito") String tipoCredito,
            @Param("plazo") Integer plazo,
            @Param("tasa") BigDecimal tasa,
            @Param("bonif_aplica") String bonifAplica,
            @Param("datos_avales") String datosAvales,
            @Param("datos_propiedad") String datosPropiedad,
            @Param("direccion") String direccion,
            @Param("Resultado") String Resultado
    );


}
