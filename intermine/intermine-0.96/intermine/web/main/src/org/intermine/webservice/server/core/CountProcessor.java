package org.intermine.webservice.server.core;

import java.util.Arrays;

import org.intermine.webservice.server.output.Output;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

public class CountProcessor
{
    public CountProcessor() {
        // Empty constructor
    }

    public void writeCount(int count, Output output) {
        output.addResultItem(Arrays.asList(String.valueOf(count)));
    }
}
