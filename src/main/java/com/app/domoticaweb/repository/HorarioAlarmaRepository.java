package com.app.domoticaweb.repository;

import com.app.domoticaweb.model.HorarioAlarma;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HorarioAlarmaRepository extends JpaRepository<HorarioAlarma, Long> {
    HorarioAlarma findByVariableId(Long variableId);
}
