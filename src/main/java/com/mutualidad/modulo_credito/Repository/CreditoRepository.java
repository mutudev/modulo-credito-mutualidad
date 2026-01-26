package com.mutualidad.modulo_credito.Repository;

import com.mutualidad.modulo_credito.Models.ModelCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CreditoRepository extends JpaRepository<ModelCredito, Integer> {

    ModelCredito findById(int id);

    @Query(value = "SELECT * FROM VW_AVAL_CREDITO WHERE ID_CREDITO = :idCredito ", nativeQuery = true)
    List<Object[]> traerAvales(@Param("idCredito") int idCredito);



    @Query(value = "SELECT COUNT (ID_CREDITO) FROM VW_AVAL_CREDITO WHERE ID_CREDITO = :idCredito ", nativeQuery = true)
    int traerNumAvales(@Param("idCredito") int idCredito);
}
