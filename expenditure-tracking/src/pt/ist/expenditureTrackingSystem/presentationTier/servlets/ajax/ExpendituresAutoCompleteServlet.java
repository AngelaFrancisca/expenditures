package pt.ist.expenditureTrackingSystem.presentationTier.servlets.ajax;

import java.util.Collection;
import java.util.Map;

import pt.ist.expenditureTrackingSystem.presentationTier.renderers.autoCompleteProvider.AutoCompleteProvider;
import pt.ist.fenixWebFramework.servlets.ajax.AutoCompleteServlet;

public class ExpendituresAutoCompleteServlet extends AutoCompleteServlet {

    @Override
    protected Collection getSearchResult(Map<String, String> argsMap, String value, int maxCount) {
	AutoCompleteProvider provider = getProvider(argsMap.get("provider"));
	return provider.getSearchResults(argsMap, value, maxCount);
    }

    private AutoCompleteProvider getProvider(String providerClass) {
	try {
	    Class provider = Class.forName(providerClass);
	    return (AutoCompleteProvider) provider.newInstance();
	} catch (Exception e) {
	    throw new RuntimeException("cannot find provider " + providerClass);
	}
    }

}
