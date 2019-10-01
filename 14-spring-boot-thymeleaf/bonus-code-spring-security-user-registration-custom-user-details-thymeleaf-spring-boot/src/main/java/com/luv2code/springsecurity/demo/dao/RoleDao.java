package com.luv2code.springsecurity.demo.dao;

import com.luv2code.springsecurity.demo.entity.Role;

public interface RoleDao {

	public Role findRoleByName(String theRoleName);
	
}
