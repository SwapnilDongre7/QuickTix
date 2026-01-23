package com.ticketbooking.identity.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
	@RequestMapping("/test")
	public class TestController {

	    @GetMapping("/user")
	    @PreAuthorize("hasRole('USER')")
	    public String userAccess() {
	        return "USER access granted";
	    }

	    @GetMapping("/admin")
	    @PreAuthorize("hasRole('ADMIN')")
	    public String adminAccess() {
	        return "ADMIN access granted";
	    }

	    @GetMapping("/owner")
	    @PreAuthorize("hasRole('THEATRE_OWNER')")
	    public String ownerAccess() {
	        return "THEATRE OWNER access granted";
	    }

	    @GetMapping("/admin-or-owner")
	    @PreAuthorize("hasAnyRole('ADMIN','THEATRE_OWNER')")
	    public String adminOrOwner() {
	        return "ADMIN or OWNER access granted";
	    }
	}

