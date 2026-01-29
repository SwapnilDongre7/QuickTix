package com.ticketbooking.identity.repository;

import com.ticketbooking.identity.entity.UserRole;
import com.ticketbooking.identity.entity.UserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UserRoleId> {
}
