package org.sakaiproject.nakamura.message.search;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.SlingHttpServletRequest;
import org.sakaiproject.nakamura.api.search.SearchConstants;
import org.sakaiproject.nakamura.api.search.solr.SolrSearchPropertyProvider;

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
    public void loadUserProperties(SlingHttpServletRequest request,
                                   Map<String, String> propertiesMap)
    {
        final String
            userId = request.getParameter("_userId");

        //get list of user contacts
        
    }
}
