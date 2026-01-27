package com.mutualidad.modulo_credito.Repository;

import com.mutualidad.modulo_credito.Models.ModelCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface CreditoRepository extends JpaRepository<ModelCredito, Integer> {

    ModelCredito findById(int id);

    @Query(value = "SELECT * FROM VW_AVAL_CREDITO WHERE ID_CREDITO = :idCredito ", nativeQuery = true)
    List<Object[]> traerAvales(@Param("idCredito") int idCredito);

    @Query(value = "SELECT COUNT (ID_CREDITO) FROM VW_AVAL_CREDITO WHERE ID_CREDITO = :idCredito ", nativeQuery = true)
    int traerNumAvales(@Param("idCredito") int idCredito);

    List<ModelCredito> findBySocioAndEmpresa(int socio, String empresa);

    List<ModelCredito> findBySocio(int socio);

    @Query(value = "SELECT * FROM HISTORIAL_CREDITO_GRADUALIDAD WHERE CREDITO_ID = :idCredito ", nativeQuery = true)
    List<Object[]> traerCreditosParaGradualidades(@Param("idCredito") int idCredito);

    @Query(value = "SELECT * FROM VW_HISTORIAL_CREDITO_GRADUALIDAD WHERE SOCIO = :numSocio ", nativeQuery = true)
    List<Object[]> traerCreditosParaGradualidadesPorNumSocio(@Param("numSocio") int numSocio);

    @Query(value = "SELECT TOP 1 * FROM VW_HISTORIAL_CREDITO_GRADUALIDAD WHERE SOCIO = :numSocio ORDER BY CREDITO_ID DESC", nativeQuery = true)
    List<Object[]> chequeoGradualidadesReinicio(@Param("numSocio") int numSocio);






}
