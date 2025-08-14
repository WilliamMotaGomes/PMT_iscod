package com.william.pmt.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.william.pmt.model.*;
import java.util.*;

public interface ProjectMembershipRepository extends JpaRepository<ProjectMembership, Long>
{
    Optional<ProjectMembership> findByProjectIdAndUserId(Long projectId, Long userId);

    List<ProjectMembership> findByProjectId(Long projectId);

    List<ProjectMembership> findByUserId(Long userId);
}
