package com.mutualidad.modulo_credito.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Data
@Table(name = "SOCIO")
@NamedStoredProcedureQuery(
        name = "Socio.pa_BuscarSocioXNumeroParaCredito",
        procedureName = "pa_BuscarSocioXNumeroParaCredito",
        parameters = {
                @StoredProcedureParameter(
                        mode = ParameterMode.IN,
                        name = "NumSocio",
                        type = Integer.class
                ),
                @StoredProcedureParameter(
                        mode = ParameterMode.OUT,
                        name = "NombreFormateado",
                        type = String.class
                ),
                @StoredProcedureParameter(
                        mode = ParameterMode.OUT,
                        name = "NumSocioEncontrado",
                        type = Integer.class
                ),
                @StoredProcedureParameter(
                        mode = ParameterMode.OUT,
                        name = "AhorrosAntesPs",
                        type = BigDecimal.class
                ),
                @StoredProcedureParameter(
                        mode = ParameterMode.OUT,
                        name = "psMut",
                        type = BigDecimal.class
                ),
                @StoredProcedureParameter(
                        mode = ParameterMode.OUT,
                        name = "psNgu",
                        type = BigDecimal.class
                ),
                @StoredProcedureParameter(
                        mode = ParameterMode.OUT,
                        name = "AhorrosTotal",
                        type = BigDecimal.class
                ),
                @StoredProcedureParameter(
                        mode = ParameterMode.OUT,
                        name = "Gradualidad",
                        type = BigDecimal.class
                ),
                @StoredProcedureParameter(
                        mode = ParameterMode.OUT,
                        name = "Empresa",
                        type = String.class
                ),
                @StoredProcedureParameter(
                        mode = ParameterMode.OUT,
                        name = "Resultado",
                        type = String.class
                )
        }
)

public class ModelSocio {

    @Id
    @Column(name = "NUM_SOCIO")
    private Integer numSocio;

    @Column(name = "EMPRESA_COD")
    private String Empresa;

    @Column(name = "NOMBRES")
    private String Nombre;

    @Column(name = "APELLIDO_P")
    private String ApellidoP;

    @Column(name = "APELLIDO_M")
    private String ApellidoM;

    @Column(name = "DIRECCION")
    private String Direccion;

    @Column(name = "CAT_ESTADO_ID")
    private String Estado;

    @Column(name = "CAT_MUNICIPIO_ID")
    private String Municipio;


}
