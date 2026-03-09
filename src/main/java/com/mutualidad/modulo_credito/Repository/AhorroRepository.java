package com.mutualidad.modulo_credito.Repository;

import com.mutualidad.modulo_credito.Models.ModelAhorro;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AhorroRepository extends JpaRepository<ModelAhorro, Integer> {

    ModelAhorro findBySocio(int socio);





}
