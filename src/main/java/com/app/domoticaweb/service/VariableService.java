package com.app.domoticaweb.service;

import com.app.domoticaweb.model.Variable;

import java.util.List;
import java.util.Optional;

public interface VariableService {

    Variable crearVariable(Variable variable);
    Optional<Variable> obtenerVariablePorId(Long id);
    Optional<Variable> obtenerVariablePorNombre(String nombre);
    List<Variable> obtenerTodasLasVariables();
    Variable actualizarVariable(Variable variable);
    void eliminarVariable(Long id);

}
