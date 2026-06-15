package com.mutualidad.modulo_credito.Repository;

import com.mutualidad.modulo_credito.Models.ModelConfiguracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfiguracionRepository extends JpaRepository<ModelConfiguracion, Integer> {

}
