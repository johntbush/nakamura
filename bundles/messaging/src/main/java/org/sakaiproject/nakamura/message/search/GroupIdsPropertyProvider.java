package org.sakaiproject.nakamura.message.search;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.authorizable.Authorizable;
import org.sakaiproject.nakamura.api.lite.authorizable.AuthorizableManager;
import org.sakaiproject.nakamura.api.lite.authorizable.Group;
import org.sakaiproject.nakamura.api.search.SearchConstants;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchPropertyProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;

/**
 * User: duffy
 * Date: Sep 21, 2011
 * Time: 9:41:38 AM
 */
@Component
@Service
@Property(name = SearchConstants.REG_PROVIDER_NAMES, value = "GroupIdsPropertyProvider")
public class GroupIdsPropertyProvider
    implements SolrSearchPropertyProvider
{
    private static final Logger
        LOG                                 = LoggerFactory.getLogger(GroupIdsPropertyProvider.class);

    @Reference
    protected transient Repository
        repository                          = null;

    public static final String
        GROUP_IDS                           = "_knownGroupIds";
    
    public void loadUserProperties(SlingHttpServletRequest request,
                                   Map<String, String> propertiesMap)
    {
        LOG.debug ("expanding user Group IDs");
        
        final String
            userId = propertiesMap.get("_userId");
        Session
            session = null;
        AuthorizableManager
            authorizableManager = null;
        Authorizable
            user = null;

        if (userId == null)
        {
            LOG.error ("userId not supplied to expand group Ids");
            return;
        }

        try
        {
            session = repository.loginAdministrative();
            authorizableManager = session.getAuthorizableManager();

            user = authorizableManager.findAuthorizable(userId);
        }
        catch (Exception e)
        {
            LOG.error ("could not retrieve user object from AuthorizableManger");
            return;
        }

        Iterator<Group>
            groups = user.memberOf(authorizableManager);

        Group
            group = null;
        StringBuilder
            sb = new StringBuilder();
        String
            delim = "";

        sb.append("AND ");

        if (groups == null || !groups.hasNext())
        {
            LOG.debug("no groups recovered");
            sb.append("!id:*");
        }
        else
        {
            sb.append("id:(");
            while (groups.hasNext())
            {
                group = groups.next();
                sb.append(delim).append(group.getId());
                delim = " OR ";
            }
            sb.append(")");
        }

        LOG.debug("group Ids to append to query: [" + sb.toString() + "]");

        propertiesMap.put(GROUP_IDS, sb.toString());
    }
}
