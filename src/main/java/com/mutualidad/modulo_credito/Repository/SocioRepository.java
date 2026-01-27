package com.mutualidad.modulo_credito.Repository;

import com.mutualidad.modulo_credito.Models.ModelEmpresa;
import com.mutualidad.modulo_credito.Models.ModelSocio;
import com.mutualidad.modulo_credito.Models.ModelSolicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Repository
public interface SocioRepository extends JpaRepository<ModelSocio, Integer> {

    ModelSocio  findByNumSocio(int numSocio);

    @Query(value = "SELECT * FROM VW_SOCIO_INFO WHERE NUM_SOCIO = :numSocio", nativeQuery = true)
    List<Object[]> traerDetalleSocio(@Param("numSocio") int numSocio);

    @Procedure(name = "Socio.pa_BuscarSocioXNumeroParaCredito")
    HashMap pa_BuscarSocioXNumeroParaCredito(int NumSocio, String NombreFormateado, int NumSocioEncontrado,
                                             BigDecimal AhorrosAntesPs, BigDecimal psMut, BigDecimal psNgu,
                                             BigDecimal AhorrosTotal, BigDecimal Gradualidad, String Empresa, String Resultado);


    //Traer cuando es menos de sus ahorros y aplica PACSE (VA A SOLICITAR MÁS DE 200K Y TIENE MÁS EN SU AHORRO)
    @Query(value = "SELECT * FROM TIPO_CREDITO", nativeQuery = true)
    List<Object[]> traerTiposCreditoCaso1();

    @Query(value = "SELECT * FROM CAT_TASAS", nativeQuery = true)
    List<Object[]> traerTasas();

    @Query(value = "SELECT * FROM CAT_PLAZOS", nativeQuery = true)
    List<Object[]> traerPlazos();

    @Query(value = "SELECT * FROM CAT_GRADUALIDADES WHERE ID = 2 OR ID = 3", nativeQuery = true)
    List<Object[]> traerGradualidadesMut();

    @Query(value = "SELECT * FROM CAT_GRADUALIDADES", nativeQuery = true)
    List<Object[]> traerGradualidadesNgu();

    @Query(value = "SELECT * FROM CAT_IDENTIFICACIONES", nativeQuery = true)
    List<Object[]> traerIdsAvales();

    @Query(value = "SELECT * FROM CAT_ACREDITACIONES", nativeQuery = true)
    List<Object[]> traerAcreditaciones();

    @Query(value = " SELECT * FROM VW_SOCIO_INFO WHERE NUM_SOCIO = :numSocio ", nativeQuery = true)
    Object[] traerInfoSocio(@Param("numSocio") int numSocio);


    @Query(value = "SELECT * FROM CAT_PARENTESCO", nativeQuery = true)
    List<Object[]> traerParentescos();

    @Query(value = "SELECT * FROM CAT_TIPO_RIESGO", nativeQuery = true)
    List<Object[]> traerTiposRiesgo();

    @Query(value = "SELECT (PRIMER_NOM + ' ' + SEGUNDO_NOM + ' ' + APELLIDO_P + ' ' + APELLIDO_M) AS NOMBRE FROM SOCIO WHERE NUM_SOCIO = :numSocio", nativeQuery = true)
    String traerNombreSocio(@Param("numSocio") int numSocio);

    @Query(
            value =
                    "SELECT " +
                            "    (PRIMER_NOM + ' ' + SEGUNDO_NOM + ' ' + APELLIDO_P + ' ' + APELLIDO_M) AS NOMBRE, " +
                            "    NUM_SOCIO, " +
                            "    TIPO_SOCIO.DESCRIPCION, " +
                            "    EMPRESA.NOMBRE " +
                            "FROM SOCIO " +
                            "INNER JOIN TIPO_SOCIO ON TIPO_SOCIO.ID = SOCIO.CAT_TIPO_ID " +
                            "INNER JOIN EMPRESA ON EMPRESA.CODIGO = SOCIO.EMPRESA_COD " +
                            "WHERE STATUS = 1 " +
                            "AND (PRIMER_NOM + ' ' + SEGUNDO_NOM + ' ' + APELLIDO_P + ' ' + APELLIDO_M) LIKE '%' + :nombreCompleto + '%'",
            nativeQuery = true
    )
    List<Object[]> buscarPorNombreCompleto(@Param("nombreCompleto") String nombreCompleto);

    // OPCIÓN 2: Para búsqueda de 2 palabras específicas (más eficiente)
    @Query(
            value =
                    "SELECT " +
                            "    (PRIMER_NOM + ' ' + SEGUNDO_NOM + ' ' + APELLIDO_P + ' ' + APELLIDO_M) AS NOMBRE, " +
                            "    NUM_SOCIO, " +
                            "    TIPO_SOCIO.DESCRIPCION, " +
                            "    EMPRESA.NOMBRE " +
                            "FROM SOCIO " +
                            "INNER JOIN TIPO_SOCIO ON TIPO_SOCIO.ID = SOCIO.CAT_TIPO_ID " +
                            "INNER JOIN EMPRESA ON EMPRESA.CODIGO = SOCIO.EMPRESA_COD " +
                            "WHERE STATUS = 1 " +
                            "AND CHARINDEX(:palabra1, PRIMER_NOM + ' ' + SEGUNDO_NOM + ' ' + APELLIDO_P + ' ' + APELLIDO_M) > 0 " +
                            "AND CHARINDEX(:palabra2, PRIMER_NOM + ' ' + SEGUNDO_NOM + ' ' + APELLIDO_P + ' ' + APELLIDO_M) > 0",
            nativeQuery = true
    )
    List<Object[]> buscarPorDospalabras(@Param("palabra1") String palabra1, @Param("palabra2") String palabra2);

    // Query adicional para 3 palabras
    @Query(
            value =
                    "SELECT " +
                            "    (PRIMER_NOM + ' ' + SEGUNDO_NOM + ' ' + APELLIDO_P + ' ' + APELLIDO_M) AS NOMBRE, " +
                            "    NUM_SOCIO, " +
                            "    TIPO_SOCIO.DESCRIPCION, " +
                            "    EMPRESA.NOMBRE " +
                            "FROM SOCIO " +
                            "INNER JOIN TIPO_SOCIO ON TIPO_SOCIO.ID = SOCIO.CAT_TIPO_ID " +
                            "INNER JOIN EMPRESA ON EMPRESA.CODIGO = SOCIO.EMPRESA_COD " +
                            "WHERE STATUS = 1 " +
                            "AND CHARINDEX(:palabra1, PRIMER_NOM + ' ' + SEGUNDO_NOM + ' ' + APELLIDO_P + ' ' + APELLIDO_M) > 0 " +
                            "AND CHARINDEX(:palabra2, PRIMER_NOM + ' ' + SEGUNDO_NOM + ' ' + APELLIDO_P + ' ' + APELLIDO_M) > 0 " +
                            "AND CHARINDEX(:palabra3, PRIMER_NOM + ' ' + SEGUNDO_NOM + ' ' + APELLIDO_P + ' ' + APELLIDO_M) > 0",
            nativeQuery = true
    )
    List<Object[]> buscarPorTresPalabras(@Param("palabra1") String palabra1, @Param("palabra2") String palabra2, @Param("palabra3") String palabra3);





}
