package com.mutualidad.modulo_credito.Services;

import com.mutualidad.modulo_credito.Models.*;
import com.mutualidad.modulo_credito.Repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

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

    @Autowired
    private AhorroRepository repoAhorro;


    @PersistenceContext
    private EntityManager entityManager;


    @Transactional
    public HashMap validarLogin(String usuario, String password, String resultado, int rol) {
        return repoUsuario.pa_validarLogin(usuario, password, resultado, rol);
    }


    public List<Object[]> obtenerHistorialPagos(int creditoId) {
        return repoCredito.pa_historialCreditos(creditoId);
    }


    @Transactional
    public List<Map<String, Object>> obtenerCarteras(int opcion, String empresa) {

        StoredProcedureQuery sp = entityManager
                .createStoredProcedureQuery("pa_ObtenerCarteras");

        sp.registerStoredProcedureParameter("opcion", Integer.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("empresa", String.class, ParameterMode.IN);

        sp.setParameter("opcion", opcion);
        sp.setParameter("empresa", empresa);

        List<Object[]> result = sp.getResultList();

        List<Map<String, Object>> lista = new ArrayList<>();
        int indice = 1;

        for (Object[] row : result) {
            Map<String, Object> map = new HashMap<>();
            map.put("indice", indice);
            map.put("IdCredito", row[0]);
            map.put("empresa", row[1]);
            map.put("num_socio", row[2]);
            map.put("nombre_socio", row[3]);
            map.put("fecha_desembolso", row[4]);
            map.put("monto_desembolso", row[5]);
            map.put("fecha_cuota_pendiente", row[6]);
            map.put("cuotas", row[7]);
            map.put("tasa", row[8]);
            map.put("saldo_credito", row[9]);
            map.put("fecha_ultima_pago", row[10]);
            indice++;
            lista.add(map);
        }

        return lista;
    }

    @Transactional
    public List<Map<String, Object>> obtenerCreditos(String fechaInicio, String fechaFin, String codEmpresa) {

        StoredProcedureQuery sp = entityManager
                .createStoredProcedureQuery("pa_ObtenerCreditosMes");

        sp.registerStoredProcedureParameter("fecha_inicio", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("fecha_fin", String.class, ParameterMode.IN);
        sp.registerStoredProcedureParameter("empresa", String.class, ParameterMode.IN);

        sp.setParameter("fecha_inicio", fechaInicio);
        sp.setParameter("fecha_fin", fechaFin);
        sp.setParameter("empresa", codEmpresa);

        List<Object[]> result = sp.getResultList();

        List<Map<String, Object>> lista = new ArrayList<>();
        int indice = 1;

        for (Object[] row : result) {
            Map<String, Object> map = new HashMap<>();
            map.put("indice", indice);
            map.put("IdCredito", row[0]);
            map.put("empresa", row[1]);
            map.put("num_socio", row[2]);
            map.put("nombre_socio", row[3]);
            map.put("fecha_desembolso", row[4]);
            map.put("monto_desembolso", row[5]);
            map.put("tipo", row[6]);
            map.put("tasa", row[7]);

            indice++;

            lista.add(map);
        }

        return lista;
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

    public ModelTipoCredito encontrarTipoCreditoConId(int id) {
        return repoTipoCredito.findById(id);
    }

    public List<ModelCredito> encontrarCreditosConSocio(int socio) {
        return repoCredito.findBySocio(socio);
    }

    public List<Object[]> traerCreditosParaGradualidades(int idCredito) {
        return repoCredito.traerCreditosParaGradualidades(idCredito);
    }

    public List<Object[]> traerCreditosParaGradualidadesPorNumSocio(int numSocio) {
        return repoCredito.traerCreditosParaGradualidadesPorNumSocio(numSocio);
    }

    public List<Object[]> chequeoGradualidadesReinicio(int numSocio) {
        return repoCredito.chequeoGradualidadesReinicio(numSocio);
    }

    public ModelSolicitud obtenerSoliXId(int id) {
        return repoSolicitud.findById(id);
    }


    public List<ModelSolicitud> obtenerSolicitudesPendientes(int estado) {
        return repoSolicitud.findByEstado(estado);
    }

    public List<ModelCredito> obtenerParaReimpresion(int socio, String empresa) {
        return repoCredito.findBySocioAndEmpresa(socio, empresa);
    }

    public List<ModelCredito> obtenerParaEstadoDeCuenta(int socio, String empresa, int status) {
        return repoCredito.findBySocioAndEmpresaAndStatus(socio, empresa, status);
    }

    public double chequeoSaldo( int numCredito) {
        return repoCredito.chequeoSaldo(numCredito);
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

    public ModelAhorro traerAhorro(int numero) {return repoAhorro.findBySocio(numero);}

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
