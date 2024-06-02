package org.example.repositories;

import org.example.domain.ImageFiltersRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ImageFiltersRequestRepository extends JpaRepository<ImageFiltersRequest, UUID> {
}
