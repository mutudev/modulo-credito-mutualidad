package com.mutualidad.modulo_credito.Repository;

import com.mutualidad.modulo_credito.Models.ModelEmpresa;
import com.mutualidad.modulo_credito.Models.ModelTipoCredito;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoCreditoRepository extends JpaRepository<ModelTipoCredito, Integer> {

    ModelTipoCredito findByNombre(String nombre);

    ModelTipoCredito findById(int Id);


}
