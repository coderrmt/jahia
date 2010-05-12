package org.jahia.ajax.gwt.client.widget.toolbar.action;

import com.google.gwt.http.client.*;
import org.jahia.ajax.gwt.client.core.JahiaGWTParameters;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.widget.LinkerSelectionContext;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: david
 * Date: Apr 28, 2010
 * Time: 2:26:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExecuteActionItem extends BaseActionItem {
    public static final int STATUS_CODE_OK = 200;
    private String action;

    public void onComponentSelection() {
        final List<GWTJahiaNode> gwtJahiaNodes = linker.getSelectedNodes();
        for (GWTJahiaNode gwtJahiaNode : gwtJahiaNodes) {
            String baseURL = org.jahia.ajax.gwt.client.util.URL.getAbsoluteURL(JahiaGWTParameters.getContextPath() + "/cms/render");
            String localURL = baseURL + "/default/" + JahiaGWTParameters.getLanguage() + gwtJahiaNode.getPath();
            linker.loading("Executing action ...");
            RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, localURL + "." + action + ".do");
            try {
                Request response = builder.sendRequest(null, new RequestCallback() {
                    public void onError(Request request, Throwable exception) {
                        com.google.gwt.user.client.Window.alert("Cannot create connection");
                        linker.loaded();
                    }

                    public void onResponseReceived(Request request, Response response) {
                        if (response.getStatusCode() != 200) {
                            com.google.gwt.user.client.Window.alert("Cannot contact remote server : error "+response.getStatusCode());
                        }
                        linker.loaded();
                    }
                });


            } catch (RequestException e) {
                // Code omitted for clarity
            }
        }
    }

    public void handleNewLinkerSelection() {
        LinkerSelectionContext lh = linker.getSelectionContext();

        setEnabled(lh.isTableSelection() || lh.isMainSelection());
    }

    public void setAction(String action) {
        this.action = action;
    }
}

