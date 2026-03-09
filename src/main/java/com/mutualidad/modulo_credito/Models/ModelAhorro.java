package com.mutualidad.modulo_credito.Models;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
@Table(name = "CUENTA_AHORRO")
public class ModelAhorro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "SOCIO")
    private int socio;

    @Column(name = "NUM_CUENTA")
    private String num_cuenta;

    @Column(name = "SALDO")
    private double saldo;

    @Column(name = "SALDO_CONGELADO")
    private double saldo_congelado;

    @Column(name = "STATUS")
    private int status;

    @Column(name = "FC")
    private Date fc;
}
