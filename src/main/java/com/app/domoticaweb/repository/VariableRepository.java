package com.app.domoticaweb.repository;

import com.app.domoticaweb.model.Variable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VariableRepository extends JpaRepository<Variable, Long> {

    Optional<Variable> findByNombre(String nombre);
}
