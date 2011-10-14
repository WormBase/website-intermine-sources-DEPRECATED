package org.intermine.bio.webservice;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.intermine.api.InterMineAPI;
import org.intermine.api.profile.Profile;
import org.intermine.api.query.PathQueryExecutor;
import org.intermine.api.results.ExportResultsIterator;
import org.intermine.bio.web.export.SequenceExporter;
import org.intermine.metadata.ClassDescriptor;
import org.intermine.objectstore.ObjectStore;
import org.intermine.pathquery.Path;
import org.intermine.pathquery.PathException;
import org.intermine.pathquery.PathQuery;
import org.intermine.util.StringUtil;
import org.intermine.web.logic.Constants;
import org.intermine.web.logic.export.Exporter;
import org.intermine.web.logic.export.ResponseUtil;
import org.intermine.web.logic.session.SessionMethods;
import org.intermine.webservice.server.WebServiceRequestParser;
import org.intermine.webservice.server.exceptions.BadRequestException;
import org.intermine.webservice.server.exceptions.InternalErrorException;
import org.intermine.webservice.server.output.Output;
import org.intermine.webservice.server.output.StreamedOutput;
import org.intermine.webservice.server.output.TabFormatter;
import org.intermine.webservice.server.query.AbstractQueryService;
import org.intermine.webservice.server.query.result.PathQueryBuilder;

/**
 * A service for exporting query results as fasta.
 * @author Alexis Kalderimis.
 *
 */
public class FastaQueryService extends AbstractQueryService
{

    private static final String XML_PARAM = "query";

    /**
     * Constructor.
     * @param im A reference to an InterMine API settings bundle.
     */
    public FastaQueryService(InterMineAPI im) {
        super(im);
    }

    @Override
    protected String getDefaultFileName() {
        return "results" + StringUtil.uniqueString() + ".fa";
    }

    protected OutputStream os;

    @Override
    protected Output getDefaultOutput(PrintWriter out, OutputStream os) {
        this.os = os;
        output = new StreamedOutput(out, new TabFormatter());
        if (isUncompressed()) {
            ResponseUtil.setPlainTextHeader(response,
                    getDefaultFileName());
        }
        return output;
    }

    @Override
    public int getFormat() {
        return UNKNOWN_FORMAT;
    }

    @Override
    protected void execute(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        HttpSession session = request.getSession();

        PathQuery pathQuery = getQuery();
        int index = 0;

        Exporter exporter;
        try {
            ObjectStore objStore = im.getObjectStore();
            exporter = new SequenceExporter(objStore, os, index, im.getClassKeys());
            ExportResultsIterator iter = null;
            try {
                Profile profile = SessionMethods.getProfile(session);
                PathQueryExecutor executor = this.im.getPathQueryExecutor(profile);
                iter = executor.execute(pathQuery, 0, WebServiceRequestParser.DEFAULT_MAX_COUNT);
                iter.goFaster();
                exporter.export(iter);
            } finally {
                if (iter != null) {
                    iter.releaseGoFaster();
                }
            }
        } catch (Exception e) {
            throw new InternalErrorException("Service failed:" + e, e);
        }

    }


    /**
     * Return the query specified in the request, shorn of all duplicate
     * classes in the view. Note, it is the users responsibility to ensure
     * that there are only SequenceFeatures in the view.
     * @param request The HTTP request
     * @return A suitable pathquery for getting GFF3 data from.
     */
    protected PathQuery getQuery() {
        String xml = request.getParameter(XML_PARAM);

        if (StringUtils.isEmpty(xml)) {
            throw new BadRequestException("query is blank");
        }

        PathQueryBuilder builder = getQueryBuilder(xml, request);
        PathQuery pq = builder.getQuery();

        if (pq.getView().size() > 1) {
            throw new BadRequestException("Queries for this webservice may only have one output column");
        }

        return pq;
    }

}
