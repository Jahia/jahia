package org.jahia.tools.contentgenerator.junit;

import static org.junit.Assert.assertEquals;

import org.jahia.tools.contentgenerator.PageService;
import org.jahia.tools.contentgenerator.UserGroupService;
import org.junit.Before;
import org.junit.Test;

public class UserGroupServiceTest extends ContentGeneratorTestCase{

	private static UserGroupService userService;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		userService = new UserGroupService();
	}

	@Test
	public void testGetNbUsersPerGroup() {
		Integer nbGroups = new Integer(10);
		Integer nbUsers = new Integer(550);
		
		Integer nbUsersPerGroup = userService.getNbUsersPerGroup(nbUsers, nbGroups);
		assertEquals(Integer.valueOf(55), nbUsersPerGroup);
		
		nbGroups = new Integer(10);
		nbUsers = new Integer(558);
		
		nbUsersPerGroup = userService.getNbUsersPerGroup(nbUsers, nbGroups);
		assertEquals(Integer.valueOf(55), nbUsersPerGroup);
	}

	@Test
	public void testGetNbUsersRemaining() {
		Integer nbGroups = new Integer(10);
		Integer nbUsers = new Integer(550);
		
		Integer nbUsersLastGroup = userService.getNbUsersRemaining(nbUsers, nbGroups);
		assertEquals(Integer.valueOf(0), nbUsersLastGroup);
		
		nbGroups = new Integer(10);
		nbUsers = new Integer(558);
		
		nbUsersLastGroup = userService.getNbUsersRemaining(nbUsers, nbGroups);
		assertEquals(Integer.valueOf(8), nbUsersLastGroup);
	}
}
