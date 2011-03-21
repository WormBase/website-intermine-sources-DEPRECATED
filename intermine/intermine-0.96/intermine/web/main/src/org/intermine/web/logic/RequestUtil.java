package org.intermine.web.logic;

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


/**
 * Util methods for HttpServletRequest.
 * @author Jakub Kulaviak
 **/
public class RequestUtil
{
    /**
     * @param request request
     * @return true if request was sent by windows client
     */
    public static boolean isWindowsClient(HttpServletRequest request) {
        String header = request.getHeader("User-Agent");
        return header.matches(".*(.*Windows.*).*");
    }
}
