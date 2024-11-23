package com.app.domoticaweb.repository;

import com.app.domoticaweb.model.HorarioLucesExterior;
import org.springframework.data.jpa.repository.JpaRepository;


public interface HorarioLucesExteriorRepository extends JpaRepository<HorarioLucesExterior, Long> {

    HorarioLucesExterior findByVariableId(Long variableId);
}
