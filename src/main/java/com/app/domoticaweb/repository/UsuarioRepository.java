package com.app.domoticaweb.repository;

import com.app.domoticaweb.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);
    List<Usuario> findByRol(String rol);
    boolean existsByCorreo(String correo);
}
