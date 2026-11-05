package com.hurios.huriosbackend.repository;

import com.hurios.huriosbackend.entity.ConsultaClienteML;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConsultaClienteMLRepository extends JpaRepository<ConsultaClienteML, Long> {
    List<ConsultaClienteML> findTop50ByOrderByTimestampPrimerMensajeDesc();
}
