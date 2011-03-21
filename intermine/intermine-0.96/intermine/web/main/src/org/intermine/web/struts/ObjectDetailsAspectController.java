package org.intermine.web.struts;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.actions.TilesAction;
import org.intermine.api.profile.InterMineBag;
import org.intermine.api.search.Scope;
import org.intermine.web.logic.results.DisplayObject;

/**
 * Controller for the objectDetailsAspect tile.
 * @author Thomas Riley
 */
public class ObjectDetailsAspectController extends TilesAction
{
    /**
     * {@inheritDoc}
     */
    public ActionForward execute(ComponentContext context,
                                 ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
        DisplayObject dobj = (DisplayObject) context.getAttribute("displayObject");
        InterMineBag interMineIdBag = (InterMineBag) context.getAttribute("interMineIdBag");

        ComponentContext cc = new ComponentContext();
        cc.putAttribute("scope", Scope.GLOBAL);
        cc.putAttribute("placement", context.getAttribute("placement"));
        cc.putAttribute("displayObject", dobj);
        cc.putAttribute("interMineIdBag", interMineIdBag);

        new TemplateListController().execute(cc, mapping, form, request, response);
        return null;
    }

}
