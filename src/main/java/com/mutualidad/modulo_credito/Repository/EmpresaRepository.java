package com.mutualidad.modulo_credito.Repository;

import com.mutualidad.modulo_credito.Models.ModelEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmpresaRepository extends JpaRepository<ModelEmpresa, Integer> {

    ModelEmpresa findByCodigo(String codigo);

    ModelEmpresa findByNombre(String nombre);

}
