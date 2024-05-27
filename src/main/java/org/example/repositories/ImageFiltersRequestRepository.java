package org.example.repositories;

import org.example.domain.ImageFiltersRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageFiltersRequestRepository extends JpaRepository<ImageFiltersRequest, String> {
}
