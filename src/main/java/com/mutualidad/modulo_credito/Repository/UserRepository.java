package com.mutualidad.modulo_credito.Repository;

import com.mutualidad.modulo_credito.Models.ModelUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<ModelUsuario, Integer> {

  @Procedure(name = "Usuario.pa_ValidarLogin")
  HashMap pa_validarLogin(String Usuario, String Pass, String Resultado, int Rol);

  @Query(
      value =
          "SELECT ROL_ID, MODULO_ID, MODULO.DESCRIPCION "
              + "FROM CONF_MODULO "
              + "INNER JOIN MODULO ON MODULO.ID = CONF_MODULO.MODULO_ID "
              + "WHERE ROL_ID = :RolUsuario",
      nativeQuery = true)
  List<Object[]> traerModulos(@Param("RolUsuario") int rolUsuario);


  ModelUsuario findByUsuario(String usuario);

  List<ModelUsuario> findByRol(int rol);

  @Query(value = "SELECT NOMBRE FROM VW_DATOS_USUARIO WHERE USUARIO = :usuario", nativeQuery = true)
  String traerAsesor(
          @Param("usuario") String usuario
  );

  @Query(
          value = "SELECT c.ID, u.USUARIO " +
                  "FROM CAJA c " +
                  "INNER JOIN USUARIO u ON u.ID = c.USUARIO_ID " +
                  "WHERE c.ESTADO = :estado " +
                  "AND c.EMPRESA = :empresa " +
                  "AND FR = CAST(GETDATE() AS DATE) " +
                  "AND TURNO = :turno",
          nativeQuery = true
  )
  List<Object[]> traerCajerosParaDesembolso(@Param("estado") int estado,  @Param("empresa") String empresa, @Param("turno") String turno);





}
