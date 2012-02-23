package org.sakaiproject.nakamura.user;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Modified;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.authorizable.User;
import org.sakaiproject.nakamura.api.user.UserFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true,
		metatype = true,
		label = "SparseUserFinder",
		description = "Find users using the Sparse indexes.")
@Service
public class SparseUserFinderImpl implements UserFinder {
  
	private static final Logger LOGGER = LoggerFactory.getLogger(SparseUserFinderImpl.class);

	@Reference
	protected Repository repository;

	public static final String DEFAULT_FIELD_NAME_LOWER = "nameLower";
	@Property(value=DEFAULT_FIELD_NAME_LOWER)
	public static final String PROP_NAME_LOWER = "field.name.lowerCase";
	protected String nameLowerField;

	public static final String DEFAULT_FIELD_EMAIL_LOWER = "newemail";
	@Property(value=DEFAULT_FIELD_EMAIL_LOWER)
	public static final String PROP_EMAIL_LOWER = "field.email.lowerCase";
	protected String emailLowerField;

	@Activate
	@Modified
	public void activate(Map<String,Object> props){
		nameLowerField = PropertiesUtil.toString((String)props.get(PROP_NAME_LOWER), DEFAULT_FIELD_NAME_LOWER);
		emailLowerField = PropertiesUtil.toString((String)props.get(PROP_EMAIL_LOWER), DEFAULT_FIELD_EMAIL_LOWER);
	}

	/**
	 * do a case insensitive sparse search for user's name.
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.nakamura.api.user.UserFinder#findUsersByName(java.lang.String)
	 */
	@Override
	public Set<String> findUsersByName(String name) throws Exception {
		Set<String> userIds = findUsersByField("name", name);
		if (userIds.isEmpty()){
			userIds.addAll(findUsersByField(nameLowerField, name.toLowerCase()));
		}
		return userIds;
	}

	/**
	 * do a case insensitive sparse search for user's email 
	 * {@inheritDoc}
	 * 
	 * @see org.sakaiproject.nakamura.api.user.UserFinder#findUsersByEmail(java.lang.String)
	 */
	@Override
	public Set<String> findUsersByEmail(String email) throws Exception {
		Set<String> userIds = findUsersByField("email", email);
		if (userIds.isEmpty()){
			userIds.addAll(findUsersByField(emailLowerField, email.toLowerCase()));
		}
		return userIds;
	}

	/**
	 * using a case insensitive sparse search, determine whether one or more users of this
	 * name exist {@inheritDoc}
	 * 
	 * @see org.sakaiproject.nakamura.api.user.UserFinder#userExists(java.lang.String)
	 */
	@Override
	public boolean userExists(String name) throws Exception {
		Set<String> userIds = findUsersByName(name);
		LOGGER.debug("user with name " + name + " exists: " + !userIds.isEmpty());
		return !userIds.isEmpty();
	}

	/**
	 * using a case insensitive sparse search, determine whether one or more users with this
	 * email exists {@inheritDoc}
	 * 
	 * @see org.sakaiproject.nakamura.api.user.UserFinder#userWithEmailExists(java.lang.String)
	 */
	@Override
	public boolean userWithEmailExists(String email) throws Exception {
		Set<String> userIds = findUsersByEmail(email);
		LOGGER.debug("user with email " + email + " exists: " + !userIds.isEmpty());
		return ! userIds.isEmpty();
	}

	@Override
	public Set<String> allUsers() throws Exception {
		throw new NotImplementedException();
	}

	/**
	 * use the {@link AuthorizableManager} to search for {@link User}s by indexed field 
	 */
	protected Set<String> findUsersByField(String fieldName, String fieldValue) throws Exception {
		Set<String> userIds = new HashSet<String>();
		try {
			Session session = repository.loginAdministrative();
			Iterator<Authorizable> iterator =
				session.getAuthorizableManager().findAuthorizable(fieldName, fieldValue, User.class);
			while (iterator.hasNext()) {
				userIds.add(iterator.next().getId());
			}
			session.logout();
		} catch (Exception e) {
			LOGGER.error(
					"an error occurred while looking up the userid for " + fieldName + " = " + fieldValue, e);
		}
		LOGGER.debug("found these users by searching " + fieldName + "=" + fieldValue + " : " + userIds);
		return userIds;
	}

}
