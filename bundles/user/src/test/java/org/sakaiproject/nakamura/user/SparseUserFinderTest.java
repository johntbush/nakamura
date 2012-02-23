package org.sakaiproject.nakamura.user;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Iterator;
import java.util.Set;

import org.apache.commons.collections.IteratorUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.authorizable.User;

@RunWith(MockitoJUnitRunner.class)
public class SparseUserFinderTest {
	
	@Mock
	private Repository repository;
	@Mock
	private Session session;
	@Mock
	private AuthorizableManager am;
	@Mock
	private User user;
	
	private SparseUserFinderImpl finder;

	private static final String USER_NAME = "USERID1";
	private static final String USER_EMAIL = "USERID1@Example.Com";

	private static final String EMAIL = "email";
	private static final String NAME = "name";
	private static final String EMAIL_LOWER = "emailLower";
	private static final String NAME_LOWER = "nameLower";
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() throws Exception {
		when(user.getId()).thenReturn(USER_NAME);
		when(repository.loginAdministrative()).thenReturn(session);
		when(session.getAuthorizableManager()).thenReturn(am);
		
		Iterator<Authorizable> itr = IteratorUtils.singletonListIterator(user);
		when(am.findAuthorizable("name", "USERID1", User.class)).thenReturn(itr);
		when(am.findAuthorizable("nameLower", "userid1", User.class)).thenReturn(itr);
		
		when(am.findAuthorizable(EMAIL, USER_EMAIL, User.class)).thenReturn(itr);
		when(am.findAuthorizable(EMAIL_LOWER, USER_EMAIL.toLowerCase(), User.class)).thenReturn(itr);
		
		finder = new SparseUserFinderImpl();
		finder.emailLowerField = EMAIL_LOWER;
		finder.nameLowerField = NAME_LOWER;
		finder.repository = repository;
	}

	@Test
	public void testFindUsersByFieldDirect() throws Exception{
		Set<String> found = finder.findUsersByField(NAME, USER_NAME);
		verify(am).findAuthorizable(NAME, USER_NAME, User.class);
		verifyNoMoreInteractions(am);		
		assertEquals(1, found.size());
		assertEquals(USER_NAME, found.iterator().next());
	}

	@Test
	public void testFindUsersByAltField() throws Exception{		
		Set<String> found = finder.findUsersByField(NAME_LOWER, "userid1");
		verify(am).findAuthorizable(NAME_LOWER, "userid1", User.class);
		verifyNoMoreInteractions(am);
		assertEquals(1, found.size());
		assertEquals(USER_NAME, found.iterator().next());
	}
	
	@Test
	public void testFindUsersByName() throws Exception{
		Set<String> found = finder.findUsersByName(USER_NAME);
		verify(am).findAuthorizable(NAME, USER_NAME, User.class);
		verifyNoMoreInteractions(am);
		assertEquals(1, found.size());
		assertEquals(USER_NAME, found.iterator().next());
	}

	@Test
	public void testFindUsersByNameLower() throws Exception{
		Set<String> found = finder.findUsersByName("userid1");
		verify(am).findAuthorizable(NAME, "userid1", User.class);
		verify(am).findAuthorizable(NAME_LOWER, "userid1", User.class);
		verifyNoMoreInteractions(am);
		assertEquals(1, found.size());
		assertEquals(USER_NAME, found.iterator().next());
	}

	@Test
	public void testFindUsersByNameOffCase() throws Exception{
		String search = "UseRid1";
		Set<String> found = finder.findUsersByName(search);
		verify(am).findAuthorizable(NAME, search, User.class);
		verify(am).findAuthorizable(NAME_LOWER, search.toLowerCase(), User.class);
		verifyNoMoreInteractions(am);
		assertEquals(1, found.size());
		assertEquals(USER_NAME, found.iterator().next());
	}

	@Test
	public void testFindUsersByEmail() throws Exception{
		Set<String> found = finder.findUsersByEmail(USER_EMAIL);
		verify(am).findAuthorizable(EMAIL, USER_EMAIL, User.class);
		verifyNoMoreInteractions(am);
		assertEquals(1, found.size());
		assertEquals(USER_NAME, found.iterator().next());
	}

	@Test
	public void testFindUsersByEmailLower() throws Exception{
		String search = "userid1@example.com";
		Set<String> found = finder.findUsersByEmail(search);
		verify(am).findAuthorizable(EMAIL, search, User.class);
		verify(am).findAuthorizable(EMAIL_LOWER, search, User.class);
		verifyNoMoreInteractions(am);
		assertEquals(1, found.size());
		assertEquals(USER_NAME, found.iterator().next());
	}

	@Test
	public void testFindUsersByEmailOffCase() throws Exception{
		String search = "UseRid1@eXampLe.cOm";
		Set<String> found = finder.findUsersByEmail(search);
		verify(am).findAuthorizable(EMAIL, search, User.class);
		verify(am).findAuthorizable(EMAIL_LOWER, search.toLowerCase(), User.class);
		verifyNoMoreInteractions(am);
		assertEquals(1, found.size());
		assertEquals(USER_NAME, found.iterator().next());
	}

	@Test
	public void testUserExists() throws Exception {
		assertTrue(finder.userExists(USER_NAME));
	}

	@Test
	public void testUserExistsLower() throws Exception {
		assertTrue(finder.userExists("userid1"));
	}

	@Test
	public void testUserExistsMixed() throws Exception {
		assertTrue(finder.userExists("uSeRID1"));
	}

	@Test
	public void testUserDoesntExist() throws Exception {
		assertFalse(finder.userExists("USERID"));
		assertFalse(finder.userExists("userid"));
		assertFalse(finder.userExists("uSeRID"));
	}

	@Test
	public void testUserWithEmailExists() throws Exception {
		assertTrue(finder.userWithEmailExists(USER_EMAIL));
	}

	@Test
	public void testUserWithEmailExistsLower() throws Exception {
		assertTrue(finder.userWithEmailExists("userid1@example.com"));
	}

	@Test
	public void testUserWithEmailExistsMixed() throws Exception {
		assertTrue(finder.userWithEmailExists("uSeRID1@ExamPLE.CoM"));
	}

	@Test
	public void testUserWithEmailDoesntExist() throws Exception {
		assertFalse(finder.userWithEmailExists("USERID@Example.Com"));
		assertFalse(finder.userWithEmailExists("userid@example.com"));
		assertFalse(finder.userWithEmailExists("uSeRID@EXAmple.coM"));
	}
}
