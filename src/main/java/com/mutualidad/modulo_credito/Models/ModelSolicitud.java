package com.mutualidad.modulo_credito.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "SOLICITUD_CREDITO")
@NamedStoredProcedureQuery(
        name = "Solicitud.pa_InsertarSolicitudCredito",
        procedureName = "pa_InsertarSolicitudCredito",
        resultClasses = ModelSolicitud.class,
        parameters = {

                @StoredProcedureParameter(mode = ParameterMode.IN, name = "asesor", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "num_socio", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "tipo_credito", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "empresa", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "monto", type = BigDecimal.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "plazo", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "ahorros_alMomento", type = BigDecimal.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "tasa", type = BigDecimal.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "mora", type = BigDecimal.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "iva", type = BigDecimal.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "riesgo", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "gradualidad", type = BigDecimal.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "monto_riesgo", type = BigDecimal.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "bonif_aplica", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "tipo_riesgo", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "datos_avales", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "datos_propiedad", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "direccion", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "libre_gravamen", type = Boolean.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "acreditacion", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class)
        }
)
@NamedStoredProcedureQuery(
        name = "Solicitud.pa_CancelarSolicitudCredito",
        procedureName = "pa_CancelarSolicitudCredito",
        resultClasses = ModelSolicitud.class,
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "id_solicitud", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class)
        }
)

@NamedStoredProcedureQuery(
        name = "Solicitud.pa_ModificarSolicitud",
        procedureName = "pa_modificarSolicitud",
        resultClasses = ModelSolicitud.class,
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "id_credito", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "tipo_credito", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "plazo", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "tasa", type = BigDecimal.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "bonif_aplica", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "datos_avales", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "datos_propiedad", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "direccion", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class)
        }
)


@NamedStoredProcedureQuery(
        name = "Solicitud.pa_ConfirmarSolicitud",
        procedureName = "pa_ConfirmarSolicitud",
        resultClasses = ModelSolicitud.class,
        parameters = {
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "id_credito", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "plazo", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "tasa", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "iva", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "mora", type = Double.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "is_desembolso", type = Integer.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "monto_credito", type = BigDecimal.class),
                @StoredProcedureParameter(mode = ParameterMode.IN, name = "dictamen", type = String.class),
                @StoredProcedureParameter(mode = ParameterMode.OUT, name = "Resultado", type = String.class)
        }
)

public class ModelSolicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "ASESOR")
    private String asesor;

    @Column(name = "NUM_SOCIO")
    private Integer numSocio;

    @Column(name = "TIPO_CREDITO")
    private Integer tipoCredito;

    @Column(name = "EMPRESA_EMISORA")
    private String empresaEmisora;

    @Column(name = "MONTO")
    private BigDecimal monto;

    @Column(name = "PLAZO")
    private Integer plazo;

    @Column(name = "AHORROS_ALMOMENTO")
    private BigDecimal ahorrosAlMomento;

    @Column(name = "TASA")
    private BigDecimal tasa;

    @Column(name = "MORA")
    private BigDecimal mora;

    @Column(name = "IVA")
    private BigDecimal iva;

    @Column(name = "RIESGO")
    private Boolean riesgo;

    @Column(name = "GRADUALIDAD")
    private BigDecimal gradualidad;

    @Column(name = "MONTO_RIESGO")
    private BigDecimal montoRiesgo;

    @Column(name = "BONIF_APLICA")
    private Boolean bonifAplica;

    @Column(name = "TIPO_RIESGO")
    private Integer tipoRiesgo;

    @Column(name = "ESTADO")
    private Integer estado;

    @Column(name = "FR")
    private LocalDateTime fr;

}
