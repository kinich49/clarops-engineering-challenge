package com.clara.challenge.repositories;

import com.clara.challenge.entities.db.Trace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraceRepository extends JpaRepository<Trace, String> {}
