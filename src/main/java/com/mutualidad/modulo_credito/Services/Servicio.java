package com.mutualidad.modulo_credito.Services;

import com.mutualidad.modulo_credito.Models.*;
import com.mutualidad.modulo_credito.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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

    @Autowired
    private SolicitudRepository repoSolicitud;

    @Autowired
    private CreditoRepository repoCredito;


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

    @Transactional
    public String cancelarSolicitud(int id_solicitud, String Resultado) {
        return repoSolicitud.pa_CancelarSolicitudCredito(id_solicitud, Resultado);
    }


    @Transactional
    public String modificarSolicitud(int idCredito,String tipoCredito, int plazo, BigDecimal tasa,
                                    String bonifAplica, String datosAvales, String datosPropiedad, String direccion,
                                     String Resultado) {
        return repoSolicitud.pa_modificarSolicitud(idCredito, tipoCredito, plazo,tasa, bonifAplica, datosAvales,
                datosPropiedad, direccion, Resultado);
    }

    @Transactional
    public String confirmarSolicitud(int idCredito,int plazo, double tasa, double iva,
                                     double mora , int isDesembolso, BigDecimal montoCredito, String dictamen,  String Resultado) {
        return repoSolicitud.pa_ConfirmarSolicitud(idCredito,  plazo,tasa, iva,mora,isDesembolso,montoCredito, dictamen, Resultado);
    }

    @Transactional
    public String guardarSolicitud(
            String asesor,
            Integer numSocio,
            String tipoCredito,
            String empresa,
            BigDecimal monto,
            Integer plazo,
            BigDecimal ahorrosAlMomento,
            BigDecimal tasa,
            BigDecimal mora,
            BigDecimal iva,
            int riesgo,
            BigDecimal gradualidad,
            BigDecimal montoRiesgo,
            String bonifAplica,
            String tipoRiesgo,
            String datosAvales,
            String datosPropiedad,
            String direccion,
            Boolean libreGravamen,
            String acreditacion,
            String Resultado
    ) {

        return repoSolicitud.pa_InsertarSolicitudCredito(
                asesor,
                numSocio,
                tipoCredito,
                empresa,
                monto,
                plazo,
                ahorrosAlMomento,
                tasa,
                mora,
                iva,
                riesgo,
                gradualidad,
                montoRiesgo,
                bonifAplica,
                tipoRiesgo,
                datosAvales,
                datosPropiedad,
                direccion,
                libreGravamen,
                acreditacion,
                Resultado
        );
    }

    public ModelCredito encontrarCredito(int id) {
        return repoCredito.findById(id);
    }

    public List<Object[]> traerCajerosParaDesembolso(int estado, String empresa, String turno) {
        return repoUsuario.traerCajerosParaDesembolso(estado, empresa, turno);
    }

    public List<ModelSolicitud> obtenerSolicitudesPendientes(int estado) {
        return repoSolicitud.findByEstado(estado);
    }

    public List<ModelCredito> obtenerParaReimpresion(int socio, String empresa) {
        return repoCredito.findBySocioAndEmpresa(socio, empresa);
    }

    public  ModelEmpresa obtenerEmpresaXNombre( String nombre){
        return repoEmpresa.findByNombre(nombre);
    }

    public List<ModelEmpresa> traerCatalogoEmpresas() {
        return repoEmpresa.findAll();
    }

    public String obtenerSocioConNumero(int numSocio) {
        return repoSocio.traerNombreSocio(numSocio);
    }

    public List<Object[]> traerTiposCreditoCaso1() {
        return repoSocio.traerTiposCreditoCaso1();
    }

    public ModelSocio traerSocioXNumero(int numSocio){
        return repoSocio.findByNumSocio(numSocio);
    }

    public List<Object[]> traerDetalleSocio(int numSocio) {
        return repoSocio.traerDetalleSocio(numSocio);
    }

    public List<Object[]> traerTasas() {
        return repoSocio.traerTasas();
    }
    public Object[] traerInfoSocio(int numSocio) {
        return repoSocio.traerInfoSocio(numSocio);
    }

    public List<Object[]> traerAvales(int idCredito) {
        return repoCredito.traerAvales(idCredito);
    }


    public int traerNumAvales(int idCredito){
        return repoCredito.traerNumAvales(idCredito);
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

    public List<Object[]> traerParentescos() {
        return repoSocio.traerParentescos();
    }

    public List<Object[]> traerTiposRiesgo() {
        return repoSocio.traerTiposRiesgo();
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


    public List<Object[]> traerDatosSolicitudPendiente(int id) {return repoSolicitud.traerSolicitudPendiente(id);}


    public List<Object[]> traerAvalesSolicitudPendiente(int id) {return repoSolicitud.traerAvalesSolicitudPendiente(id);}


    public List<Object[]> traerPropietariosSolicitudPendiente(int id) {return repoSolicitud.traerPropietariosSolicitudPendiente(id);}

    public ModelEmpresa traerEmpresa(String codigo) {
        return repoEmpresa.findByCodigo(codigo);
    }

    public ModelTipoCredito traerTipoCredito(String nombre) {
        return repoTipoCredito.findByNombre(nombre);
    }


}
