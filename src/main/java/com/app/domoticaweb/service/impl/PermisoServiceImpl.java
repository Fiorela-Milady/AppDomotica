package com.app.domoticaweb.service.impl;

import com.app.domoticaweb.model.Permiso;
import com.app.domoticaweb.repository.PermisoRepository;
import com.app.domoticaweb.service.PermisoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PermisoServiceImpl implements PermisoService {


    private final PermisoRepository permisoRepository;

    @Override
    @Transactional
    public Permiso crearPermiso(Permiso permiso) {
        Optional<Permiso> permisoExistente = permisoRepository
                .findByUsuarioAndVariable(permiso.getUsuario(), permiso.getVariable());

        if (permisoExistente.isPresent()) {
            throw new RuntimeException("El permiso ya existe para el usuario y variable dados.");
        }

        return permisoRepository.save(permiso);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Permiso> obtenerPermisoPorId(Long id) {
        return permisoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permiso> obtenerPermisosPorUsuario(Long idUsuario) {
        return permisoRepository.findByUsuarioId(idUsuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permiso> obtenerPermisosPorVariable(Long idVariable) {
        return permisoRepository.findByVariableId(idVariable);
    }

    @Override
    @Transactional
    public Permiso actualizarPermiso(Permiso permiso) {
        if (!permisoRepository.existsById(permiso.getId())) {
            throw new RuntimeException("Permiso no encontrado.");
        }
        return permisoRepository.save(permiso);
    }

    @Override
    @Transactional
    public void eliminarPermiso(Long id) {
        if (!permisoRepository.existsById(id)) {
            throw new RuntimeException("Permiso no encontrado.");
        }
        permisoRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Permiso> obtenerTodosPermisos() {
        return permisoRepository.findAll();
    }

    @Override
    public boolean existePermiso(Long usuarioId, Long variableId) {
        return permisoRepository.existsByUsuarioIdAndVariableId(usuarioId, variableId);
    }

}
