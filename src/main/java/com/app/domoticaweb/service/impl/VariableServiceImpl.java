package com.app.domoticaweb.service.impl;

import com.app.domoticaweb.model.Variable;
import com.app.domoticaweb.repository.VariableRepository;
import com.app.domoticaweb.service.VariableService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class VariableServiceImpl implements VariableService {


    private final VariableRepository variableRepository;

    @Override
    public Variable crearVariable(Variable variable) {
        if (variableRepository.findByNombre(variable.getNombre()).isPresent()) {
            throw new RuntimeException("El nombre de la variable ya existe.");
        }
        return variableRepository.save(variable);
    }

    @Override
    public Optional<Variable> obtenerVariablePorId(Long id) {
        return variableRepository.findById(id);
    }

    @Override
    public Optional<Variable> obtenerVariablePorNombre(String nombre) {
        return variableRepository.findByNombre(nombre);
    }

    @Override
    public List<Variable> obtenerTodasLasVariables() {
        return variableRepository.findAll();
    }

    @Override
    public Variable actualizarVariable(Variable variable) {
        if (!variableRepository.existsById(variable.getId())) {
            throw new RuntimeException("Variable no encontrada.");
        }
        return variableRepository.save(variable);
    }

    @Override
    public void eliminarVariable(Long id) {
        if (!variableRepository.existsById(id)) {
            throw new RuntimeException("Variable no encontrada.");
        }
        variableRepository.deleteById(id);
    }
}
