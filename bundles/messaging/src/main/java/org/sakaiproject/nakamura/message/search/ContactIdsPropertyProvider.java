package org.sakaiproject.nakamura.message.search;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.sakaiproject.nakamura.api.connections.ConnectionManager;
import org.sakaiproject.nakamura.api.connections.ConnectionState;
import org.sakaiproject.nakamura.api.lite.Repository;
import org.sakaiproject.nakamura.api.lite.Session;
import org.sakaiproject.nakamura.api.lite.StorageClientException;
import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
import org.sakaiproject.nakamura.api.search.SearchConstants;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchPropertyProvider;
import org.sakaiproject.nakamura.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * User: duffy
 * Date: Sep 21, 2011
 * Time: 9:41:38 AM
 */
@Component
@Service
@Property(name = SearchConstants.REG_PROVIDER_NAMES, value = "ContactIdsPropertyProvider")
public class ContactIdsPropertyProvider
    implements SolrSearchPropertyProvider
{
    private static final Logger
        LOG                                 = LoggerFactory.getLogger(ContactIdsPropertyProvider.class);

    @Reference
    protected transient Repository
        repository                          = null;

    @Reference
    protected transient ConnectionManager
        connectionManager                   = null;

    public static final String
        CONTACT_IDS                         = "_knownUserIds";
    
    public void loadUserProperties(SlingHttpServletRequest request,
                                   Map<String, String> propertiesMap)
    {
        LOG.debug ("expanding user contact IDs");
        
        final String
            userId = propertiesMap.get("_userId");
        Session
            session = null;

        if (userId == null)
        {
            LOG.error ("userId not supplied to expand connection Ids");
            return;
        }

        try
        {
            session = repository.loginAdministrative();
        }
        catch (Exception e)
        {
            LOG.error("could not obtain a session to load user " + userId + "'s connections", e);
            return;
        }

        //get list of user contacts
        List<String>
            connectionIds = connectionManager.getConnectedUsers(session, userId, ConnectionState.ACCEPTED);

        StringBuilder
            sb = new StringBuilder();
        String
            delim = "";

        sb.append("AND id:(");

        if (connectionIds == null || connectionIds.size() < 1)
        {
            sb.append(userId);
        }
        else
        {
            for (String connectionId : connectionIds)
            {
                sb.append(delim).append(connectionId);
                delim = " OR ";
            }
        }
        
        sb.append(")");

        propertiesMap.put(CONTACT_IDS, sb.toString());
    }
}
