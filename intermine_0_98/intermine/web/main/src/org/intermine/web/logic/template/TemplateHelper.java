package org.intermine.web.logic.template;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringUtils;
import org.intermine.api.profile.InterMineBag;
import org.intermine.api.template.SwitchOffAbility;
import org.intermine.api.template.TemplateQuery;
import org.intermine.api.template.TemplateValue;
import org.intermine.api.xml.TemplateQueryBinding;
import org.intermine.objectstore.query.ConstraintOp;
import org.intermine.pathquery.PathConstraint;
import org.intermine.pathquery.PathConstraintBag;
import org.intermine.pathquery.PathConstraintLookup;
import org.intermine.pathquery.PathConstraintMultiValue;
import org.intermine.pathquery.PathConstraintNull;
import org.intermine.webservice.server.CodeTranslator;

/**
 * Static helper routines related to templates.
 *
 * @author Thomas Riley
 * @author Richard Smith
 */
public final class TemplateHelper
{
    private static final String OPERATION_PARAMETER = "op";
    private static final String EXTRA_PARAMETER = "extra";
    private static final String VALUE_PARAMETER = "value";
    private static final String ID_PARAMETER = "constraint";
    private static final String CODE_PARAMETER = "code";

    private TemplateHelper() {
        // don't
    }

    /**
     * Given a Map of TemplateQueries (mapping from template name to TemplateQuery)
     * return a string containing each template seriaised as XML. The root element
     * will be a <code>template-queries</code> element.
     *
     * @param templates  map from template name to TemplateQuery
     * @param version the version number of the XML format
     * @return  all template queries serialised as XML
     * @see  TemplateQuery
     */
    public static String templateMapToXml(Map<String, TemplateQuery> templates, int version) {
        StringWriter sw = new StringWriter();
        XMLOutputFactory factory = XMLOutputFactory.newInstance();

        try {
            XMLStreamWriter writer = factory.createXMLStreamWriter(sw);
            writer.writeStartElement("template-queries");
            for (TemplateQuery template : templates.values()) {
                TemplateQueryBinding.marshal(template, writer, version);
            }
            writer.writeEndElement();
        } catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        return sw.toString();
    }

    public static String templateMapToJson(Map<String, TemplateQuery> templates) {
        StringBuilder sb = new StringBuilder("{");
        Iterator<String> keys = templates.keySet().iterator();
        while (keys.hasNext()) {
            String name = keys.next();
            sb.append("\"" + name + "\":" + templates.get(name).toJSON());
            if (keys.hasNext()) {
                sb.append(",");
            }
        }
        sb.append("}");
        String result = sb.toString();
        return result;
    }

    /**
     * Parse templates in XML format and return a map from template name to
     * TemplateQuery.
     *
     * @param xml         the template queries in xml format
     * @param savedBags   Map from bag name to bag
     * @param version the version of the xml format, an attribute on ProfileManager
     * @return            Map from template name to TemplateQuery
     * @throws Exception  when a parse exception occurs (wrapped in a RuntimeException)
     */
    public static Map<String, TemplateQuery> xmlToTemplateMap(String xml,
            Map<String, InterMineBag> savedBags, int version) throws Exception {
        Reader templateQueriesReader = new StringReader(xml);
        return new TemplateQueryBinding().unmarshal(templateQueriesReader, savedBags, version);
    }

    /**
     * Given a HTTP request, parse out the template values.
     *
     * @param request HTTP request by user
     * @return map of constraints and values to be used to populate template.
     */
    public static Map<String, List<ConstraintInput>> parseConstraints(HttpServletRequest request) {
        // Maximum of constraints is 50, it should be enough
        Map<String, List<ConstraintInput>> ret = new HashMap<String, List<ConstraintInput>>();
        for (int i = 0; i < 50; i++) {

            String idParameter = ID_PARAMETER + i;
            String id = request.getParameter(idParameter);

            String opParameter = OPERATION_PARAMETER + i;
            String opString = request.getParameter(opParameter);
            ConstraintOp op = getConstraintOp(opParameter, opString);

            String valueParameter = VALUE_PARAMETER + i;
            String[] values = request.getParameterValues(valueParameter);
            String value = null;
            List<String> multivalues = null;
            if (values != null) {
                value = values[0];
                multivalues = Arrays.asList(values);
            }

            String extraParameter = EXTRA_PARAMETER + i;
            String extraValue = request.getParameter(extraParameter);

            String codeParameter = CODE_PARAMETER + i;
            String code = request.getParameter(codeParameter);

            if (opString != null && opString.length() > 0 && op == null) {
                throw new IllegalArgumentException("invalid parameter: '" 
                    + opParameter + "' with value '" + opString + "': "
                    + "This must be valid operation code. "
                    + "Special characters must be encoded in request. "
                    + " See help for 'url encoding'.");
            }

            if (isPresent(op) || isPresent(value) || isPresent(id) 
                    || isPresent(extraValue) || isPresent(code)
                    || multivalues != null) {
                String problemIntro = 
                    "parameters were provided for constraint " + i;
                if (!isPresent(id)) {
                    throw new IllegalArgumentException(problemIntro
                        + " but no path was provided to identify the "
                        + "constraint. Missing parameter: '" 
                        + idParameter + "'.");
                }
                if (!isPresent(op)) {
                    throw new IllegalArgumentException(problemIntro
                        + " but the operation was not specified."
                        + " Missing parameter '" + opParameter + "'.");
                }
                if (!PathConstraintNull.VALID_OPS.contains(op)
                    && (request.getParameterValues(valueParameter) == null)) {
                    throw new IllegalArgumentException(problemIntro
                        + " but no values were provided, and " + op 
                        + " requires at least one value. Missing"
                        + " parameter '" + valueParameter + "'.");
                }
                if (!PathConstraintMultiValue.VALID_OPS.contains(op)
                    && multivalues != null && multivalues.size() > 1) {
                    throw new IllegalArgumentException(
                        " An operation was provided ('" + op + "') "
                        + " that expected at most one value, but " 
                        + multivalues.size()
                        + " values were provided using the parameter '"
                        + valueParameter + "'.");
                }

                ConstraintInput load = new ConstraintInput(idParameter, 
                        id, code, op, value, multivalues, extraValue);
                if (ret.get(id) == null) {
                    ret.put(id, new ArrayList<ConstraintInput>());
                }
                ret.get(id).add(load);
            }
        }
        return ret;
    }

    private static ConstraintOp getConstraintOp(String parName, String parValue) {
        ConstraintOp ret = ConstraintOp.getConstraintOp(CodeTranslator.getCode(parValue));
        if (parValue != null && ret == null) {
            throw new IllegalArgumentException(
                    "Problem with parameter '" + parName + "': '" + parValue + "' is not a valid operator.");
        }
        return ret;
    }

    private static boolean isPresent(String value) {
        return (value != null && value.length() > 0);
    }

    private static boolean isPresent(ConstraintOp op) {
        return (op != null);
    }

    /**
     * Creates a map from input to be used later to populate the template.
     *
     * @param template template
     * @param input values from URL
     * @return map from constraints to values
     */
    public static Map<String, List<TemplateValue>> getValuesFromInput(TemplateQuery template,
            TemplateResultInput input) {
        Map<String, List<TemplateValue>> values = new HashMap<String, List<TemplateValue>>();
        for (String path : template.getEditablePaths()) {
            List<PathConstraint> constraintsForPath = template.getEditableConstraints(path);
            List<ConstraintInput> inputsForPath = new ArrayList<ConstraintInput>();
            if (input.getConstraints().get(path) != null) {
                inputsForPath.addAll(input.getConstraints().get(path));
            }

            // too many inputs for path
            if (constraintsForPath.size() < inputsForPath.size()) {
                throw new IllegalArgumentException("There were more constraints specified "
                        + " in the request than there are editable constraints for path "
                        + path + ".");
            }

            if (constraintsForPath.size() == 1) {
                // one constraint and at most one input
                PathConstraint con = constraintsForPath.get(0);
                ConstraintInput conInput = null;
                if (!inputsForPath.isEmpty()) {
                    conInput = inputsForPath.get(0);
                }
                checkAndAddValue(values, template, con, conInput, null);
            } else {
                // more than one constraint so we need to look at codes
                for (PathConstraint con : constraintsForPath) {
                    ConstraintInput foundConInput = null;
                    String code = template.getConstraints().get(con);
                    for (ConstraintInput conInput : inputsForPath) {
                        if (StringUtils.isBlank(conInput.getCode())) {
                            String err = "There are multiple editable constraints"
                                + "for path " + path + " but codes weren't set.  If there is"
                                + " more than one constraint on a path you need to specify the"
                                + " corresponding constraint codes.";
                            throw new IllegalArgumentException(err);
                        }
                        if (conInput.getCode().equals(code)) {
                            if (foundConInput != null) {
                                String err = "There was more than one constraint"
                                    + " specified with code: " + conInput.getCode()
                                    + " in the request for path: " + path + "  You should only"
                                    + " provide one value per code.";
                                throw new IllegalArgumentException(err);
                            }
                            foundConInput = conInput;
                        }
                    }
                    // foundConInput may be null but that's ok if the constraint is optional
                    checkAndAddValue(values, template, con, foundConInput, code);
                }
            }
        }
        return values;
    }

    private static void checkAndAddValue(Map<String, List<TemplateValue>> values,
            TemplateQuery template, PathConstraint con, ConstraintInput conInput, String code) {
        if (conInput != null) {
            if (template.isRequired(con)) {
                addToValuesMap(values, createTemplateValue(con, conInput, SwitchOffAbility.LOCKED));
            } else {
                addToValuesMap(values, createTemplateValue(con, conInput, SwitchOffAbility.ON));
            }
        } else if (template.isRequired(con)) {
            throw new IllegalArgumentException("There isn't a specified constraint value "
                    + "and operation for path " + con.getPath()
                    + ((code != null) ? " and code " + code : "")
                    + " in the request; this constraint is required.");
        } else {
            // no value was provided but the constraint was optional so we can do nothing
        }
    }

    private static void addToValuesMap(Map<String, List<TemplateValue>> valMap,
            TemplateValue newValue) {
        String path = newValue.getConstraint().getPath();
        List<TemplateValue> values = valMap.get(path);
        if (values == null) {
            values = new ArrayList<TemplateValue>();
            valMap.put(path, values);
        }
        values.add(newValue);
    }

    private static TemplateValue createTemplateValue(PathConstraint con, ConstraintInput input,
            SwitchOffAbility switchOffAbility) {
        TemplateValue value;
        if (con instanceof PathConstraintLookup) {
            value = new TemplateValue(con, input.getConstraintOp(), input.getValue(),
                    TemplateValue.ValueType.SIMPLE_VALUE, input.getExtraValue(), switchOffAbility);
        } else if (con instanceof PathConstraintBag) {
            value = new TemplateValue(con, input.getConstraintOp(), input.getValue(),
                    TemplateValue.ValueType.BAG_VALUE, switchOffAbility);
        } else {
            if (PathConstraintMultiValue.VALID_OPS.contains(input.getConstraintOp())) {
                value = new TemplateValue(con, input.getConstraintOp(), 
                        TemplateValue.ValueType.SIMPLE_VALUE, input.getMultivalues(), switchOffAbility);
            } else if (input.getValue() != null) {
                value = new TemplateValue(con, input.getConstraintOp(), input.getValue(),
                    TemplateValue.ValueType.SIMPLE_VALUE, switchOffAbility);
            } else {
                // For unary (null) constraints.
                value = new TemplateValue(con, input.getConstraintOp(),
                    TemplateValue.ValueType.SIMPLE_VALUE, switchOffAbility);
            }
        }
        return value;
    }
}
