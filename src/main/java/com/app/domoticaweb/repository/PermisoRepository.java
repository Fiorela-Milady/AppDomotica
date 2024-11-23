package com.app.domoticaweb.repository;

import com.app.domoticaweb.model.Permiso;
import com.app.domoticaweb.model.Usuario;
import com.app.domoticaweb.model.Variable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    Optional<Permiso> findByUsuarioAndVariable(Usuario usuario, Variable variable);
    List<Permiso> findByUsuarioId(Long usuarioId);
    List<Permiso> findByVariableId(Long variableId);
    boolean existsByUsuarioIdAndVariableId(Long usuarioId, Long variableId);
}
