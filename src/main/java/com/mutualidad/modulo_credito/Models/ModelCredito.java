package com.mutualidad.modulo_credito.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.Date;

@Entity
@Data
@Table(name = "CAT_CREDITOS")
public class ModelCredito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "ASESOR")
    private String asesor;

    @Column(name = "SOCIO")
    private int socio;

    @Column(name = "TIPO_CREDITO")
    private int tipo_credito;

    @Column(name = "EMPRESA")
    private String empresa;

    @Column(name = "MONTO")
    private double monto;

    @Column(name = "PLAZO")
    private int plazo;

    @Column(name = "FD")
    private LocalDate fd;

    @Column(name = "FV")
    private LocalDate fv;

    @Column(name = "TASA")
    private float tasa;

    @Column(name = "MORA")
    private float mora;

    @Column(name = "IVA")
    private float iva;

    @Column(name = "STATUS")
    private int status;
}
