package com.app.domoticaweb.service;


import com.app.domoticaweb.model.Permiso;
import java.util.List;
import java.util.Optional;

public interface PermisoService {
    Permiso crearPermiso(Permiso permiso);
    Optional<Permiso> obtenerPermisoPorId(Long id);
    List<Permiso> obtenerPermisosPorUsuario(Long idUsuario);
    List<Permiso> obtenerPermisosPorVariable(Long idVariable);
    Permiso actualizarPermiso(Permiso permiso);
    void eliminarPermiso(Long id);
    List<Permiso> obtenerTodosPermisos();
    boolean existePermiso(Long usuarioId, Long variableId);
}
