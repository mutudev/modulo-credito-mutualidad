package com.mutualidad.modulo_credito.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "CONFIGURACION")
@Data
public class ModelConfiguracion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "AHORRO_MENSUAL", nullable = false, precision = 18, scale = 2)
    private BigDecimal ahorroMensual;

    @Column(name = "CUOTAS_MAXIMAS_CAJERO", nullable = false)
    private Integer cuotasMaximasCajero;

    @Column(name = "APODERADO_LEGAL", length = 255)
    private String apoderadoLegal;

    @Column(name = "TESORERO_MUT", length = 255)
    private String tesoreroMut;

    @Column(name = "TESORERO_NGU", length = 255)
    private String tesoreroNgu;

    @Column(name = "TASA_IDE", precision = 12, scale = 2)
    private BigDecimal tasaIde;

    @Column(name = "FECHA_SISTEMA", nullable = false)
    private LocalDate fechaSistema;

    @Column(name = "UM", nullable = false)
    private Integer um;

    @Column(name = "FM", nullable = false)
    private LocalDate fm;

}