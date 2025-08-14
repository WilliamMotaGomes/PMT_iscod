package com.william.pmt.dto;

import com.william.pmt.model.Role;

public record SetRoleRequest(Long userId, Role role) {}
