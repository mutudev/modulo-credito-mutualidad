package com.mutualidad.modulo_credito.Services;

import com.mutualidad.modulo_credito.Models.ModelEmpresa;
import com.mutualidad.modulo_credito.Models.ModelTipoCredito;
import com.mutualidad.modulo_credito.Repository.EmpresaRepository;
import com.mutualidad.modulo_credito.Repository.SocioRepository;
import com.mutualidad.modulo_credito.Repository.TipoCreditoRepository;
import com.mutualidad.modulo_credito.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Service
public class Servicio {

    @Autowired
    private UserRepository repoUsuario;

    @Autowired
    private SocioRepository repoSocio;

    @Autowired
    private EmpresaRepository repoEmpresa;

    @Autowired
    private TipoCreditoRepository repoTipoCredito;

    @Transactional
    public HashMap validarLogin(String usuario, String password, String resultado, int rol) {
        return repoUsuario.pa_validarLogin(usuario, password, resultado, rol);
    }

    @Transactional
    public HashMap buscarSocioSolicitud(
            int NumSocio, String NombreFormateado, int NumSocioEncontrado,
            BigDecimal AhorrosAntesPs, BigDecimal psMut, BigDecimal psNgu,
            BigDecimal AhorrosTotal, BigDecimal Gradualidad, String Empresa, String Resultado) {
        return repoSocio.pa_BuscarSocioXNumeroParaCredito(NumSocio, NombreFormateado, NumSocioEncontrado, AhorrosAntesPs, psMut,
                psNgu, AhorrosTotal, Gradualidad, Empresa, Resultado);
    }

    public List<Object[]> traerTiposCreditoCaso1() {
        return repoSocio.traerTiposCreditoCaso1();
    }

    public List<Object[]> traerTasas() {
        return repoSocio.traerTasas();
    }

    public List<Object[]> traerPlazos() {
        return repoSocio.traerPlazos();
    }

    public List<Object[]> traerGradualidadesMut() {
        return repoSocio.traerGradualidadesMut();
    }

    public List<Object[]> traerAcreditaciones() {
        return repoSocio.traerAcreditaciones();
    }


    public List<Object[]> traerGradualidadesNgu() {
        return repoSocio.traerGradualidadesNgu();
    }

    public List<Object[]> buscarSocioPorNombre(String nombreCompleto) {
        String[] palabras = nombreCompleto.trim().split("\\s+");

        if (palabras.length == 1) {
            return repoSocio.buscarPorNombreCompleto(nombreCompleto);
        } else if (palabras.length == 2) {
            return repoSocio.buscarPorDospalabras(palabras[0], palabras[1]);
        } else if (palabras.length >= 3) {
            return repoSocio.buscarPorTresPalabras(palabras[0], palabras[1], palabras[2]);
        }

        return new ArrayList<>();
    }

    public List<Object[]> traerIdsAvales() {return repoSocio.traerIdsAvales();}

    public ModelEmpresa traerEmpresa(String codigo) {
        return repoEmpresa.findByCodigo(codigo);
    }

    public ModelTipoCredito traerTipoCredito(String nombre) {
        return repoTipoCredito.findByNombre(nombre);
    }


}
