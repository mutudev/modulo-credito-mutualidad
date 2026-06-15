package com.mutualidad.modulo_credito.Repository;

import com.mutualidad.modulo_credito.Models.ModelTransaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<ModelTransaccion, Integer> {

    List<ModelTransaccion> findBySocioIdAndStatusAndOperacionIdIn(int socioId, boolean status, List<Integer> operacionIds);

}
