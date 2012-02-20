package org.sakaiproject.nakamura.user;

import java.io.IOException;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import org.apache.felix.http.api.ExtHttpService;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.ComponentContext;
import org.sakaiproject.nakamura.api.user.UserFinder;
import org.sakaiproject.nakamura.formauth.FormLoginServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableMap;

@Component
public class CaseInsensitiveAuthFilter implements Filter {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CaseInsensitiveAuthFilter.class);

	/**
	 * Priority of this filter, higher number means sooner
	 */
	@Property(intValue = 8)
	protected static final String FILTER_PRIORITY_CONF = "filter.priority";

	@Reference
	protected ExtHttpService extHttpService;

	// @Reference(target="(service.pid=org.sakaiproject.nakamura.user.SolrUserFinderImpl)")
	@Reference(target="(service.pid=org.sakaiproject.nakamura.user.SparseUserFinderImpl)")
	protected UserFinder userFinder;

	protected class OverriddenRequest extends HttpServletRequestWrapper {

		private Map<String,String[]> modifiedMap;

		@SuppressWarnings("unchecked")
		public OverriddenRequest(HttpServletRequest request, Map<String,String[]> overrides) {
			super(request);
			modifiedMap = new HashMap<String, String[]>();
			modifiedMap.putAll((Map<String, String[]>)super.getParameterMap());
			modifiedMap.putAll(overrides);
		}

		@Override
		public Map<String, String[]> getParameterMap() {
			/**
			 * Deep down in the sling authentication stack the request.parameterMap is
			 * copied and used to get the parameters.
			 */
			return modifiedMap;
		}

		@Override
		public String getParameter(String parameter) {
			String[] value = modifiedMap.get(parameter);
			if (value != null){
				return value[0];
			}
			return null;
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest hrequest = (HttpServletRequest) request;

		if ("POST".equals(hrequest.getMethod())) {
			// Case Insensitive lookup during login
			if (hrequest.getRequestURI().equals("/system/sling/formlogin")) {
				String userId = (String)hrequest.getParameter((FormLoginServlet.USERNAME));
				if (userId != null) {
					try {
						Set<String> results = userFinder.findUsersByName(userId);
						if (results.size() == 1){
							// Wrap the request so it looks like the user entered the proper case
							hrequest = new OverriddenRequest(hrequest,
									ImmutableMap.of(FormLoginServlet.USERNAME,
											new String[] { results.iterator().next(), }));
						}
					} catch (Exception e) {
						LOGGER.error("An error occurred while looking up " + userId, e);
					}
				}
			}

			// Add the lowercased username as the nameLower property
			if (hrequest.getRequestURI().equals("/system/userManager/user.create.html")) {
				String name = (String)hrequest.getParameter(":name");
				if (name != null) {
					hrequest = new OverriddenRequest(hrequest,
							ImmutableMap.of("nameLower", new String[] {name.toLowerCase()}));
				}
			}
		}
		chain.doFilter(hrequest, response);
	}

	@SuppressWarnings("unchecked")
	@Activate
	protected void activate(ComponentContext componentContext)
			throws ServletException {
		Dictionary<String, Object> properties = componentContext
				.getProperties();
		int filterPriority = PropertiesUtil.toInteger(
				properties.get(FILTER_PRIORITY_CONF), 8);
		extHttpService.registerFilter(this, ".*", null, filterPriority, null);
	}

	@Deactivate
	public void deactivate(ComponentContext componentContext) {
		extHttpService.unregisterFilter(this);
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}
}