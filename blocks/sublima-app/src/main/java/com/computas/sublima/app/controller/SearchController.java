package com.computas.sublima.app.controller;

import com.computas.sublima.app.service.Form2SparqlService;
import com.computas.sublima.query.SparqlDispatcher;
import com.computas.sublima.query.service.SearchService;
import com.computas.sublima.query.service.SettingsService;
import com.hp.hpl.jena.sparql.util.StringUtils;
import org.apache.cocoon.components.flow.apples.AppleRequest;
import org.apache.cocoon.components.flow.apples.AppleResponse;
import org.apache.cocoon.components.flow.apples.StatelessAppleController;
import org.apache.cocoon.environment.Request;
import org.apache.log4j.Logger;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SearchController implements StatelessAppleController {
  private SparqlDispatcher sparqlDispatcher;
  private String mode;

  private static Logger logger = Logger.getLogger(SearchController.class);

  @SuppressWarnings("unchecked")
  public void process(AppleRequest req, AppleResponse res) throws Exception {

    this.mode = req.getSitemapParameter("mode");

    // The initial advanced search page
    if ("advancedsearch".equalsIgnoreCase(mode)) {
      res.sendPage("xhtml/search-form", null);
      return;
    }

    // If it's search-results for advanced search, topic instance or resource
    if ("resource".equalsIgnoreCase(mode) || "search-result".equalsIgnoreCase(mode)) {
      doAdvancedSearch(res, req);
      return;
    }

    if ("freetext-result".equalsIgnoreCase(mode)) {
      doFreeTextSearch(res, req);
      return;
    }

    if ("topic-instance".equalsIgnoreCase(mode)) {
      doGetTopic(res, req);
      return;
    }
  }

  private void doGetTopic(AppleResponse res, AppleRequest req) {

    String subject = "<http://sublima.computas.com/topic-instance/" + req.getSitemapParameter("topic") + ">";
    String queryString = StringUtils.join("\n", new String[]{
            "PREFIX dct: <http://purl.org/dc/terms/>",
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
            "DESCRIBE ?resource " + subject + " ?publisher ?subjects",
            "WHERE {",
            "        ?resource dct:language ?lang;",
            "				 dct:publisher ?publisher ;",
            "                dct:subject " + subject + ", ?subjects .}"});

    logger.trace("SPARQL query sent to dispatcher: " + queryString);
    Object queryResult = sparqlDispatcher.query(queryString);

    Map<String, Object> bizData = new HashMap<String, Object>();
    bizData.put("result-list", queryResult);

    String sparqlConstructQuery =
            "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
                    "prefix owl: <http://www.w3.org/2002/07/owl#>\n" +
                    "prefix dct: <http://purl.org/dc/terms/>\n" +
                    "prefix sub: <http://xmlns.computas.com/sublima#>\n" +
                    "prefix skos: <http://www.w3.org/2004/02/skos/core#>\n" +
                    "CONSTRUCT {\n" +
                    subject + " skos:prefLabel ?label ; \n" +
                    " a skos:Concept;\n" +
                    " skos:altLabel ?synLabel ;\n" +
                    " skos:related ?relSub ;\n" +
                    " skos:broader ?btSub ;\n" +
                    " skos:narrower ?ntSub .\n" +
                    " ?relSub skos:prefLabel ?relLabel ;\n" +
                    " a skos:Concept .\n" +
                    " ?btSub skos:prefLabel ?btLabel ;\n" +
                    " a skos:Concept .\n" +
                    " ?ntSub skos:prefLabel ?ntLabel ;\n" +
                    " a skos:Concept .\n" +
                    " }\n" +
                    " WHERE {\n" +
                    subject + " rdfs:label ?label .\n" +
                    subject + " a skos:Concept .\n" +
                    " OPTIONAL { " + subject + " skos:altLabel ?synLabel  . }\n" +
                    " OPTIONAL { " + subject + " skos:related ?relSub .\n" +
                    " ?relSub rdfs:label ?relLabel . }\n" +
                    " OPTIONAL { " + subject + " skos:broader ?btSub .\n" +
                    " ?btSub a skos:Concept ;\n" +
                    "     rdfs:label ?btLabel . }\n" +
                    " OPTIONAL { ?ntSub skos:broader " + subject + " .\n" +
                    " ?ntSub a skos:Concept ;\n" +
                    "     rdfs:label ?ntLabel . } }";

    logger.trace("SPARQL query sent to dispatcher: " + sparqlConstructQuery);
    queryResult = sparqlDispatcher.query(sparqlConstructQuery);

    bizData.put("navigation", queryResult);
    bizData.put("mode", mode);
    bizData.put("request", "<empty></empty>");
    res.sendPage("xml/sparql-result", bizData);
  }


  private void doFreeTextSearch(AppleResponse res, AppleRequest req) {
    String defaultBooleanOperator = SettingsService.getProperty("sublima.default.boolean.operator");
    String chosenOperator = req.getCocoonRequest().getParameter("booleanoperator");
    boolean deepsearch = false;

    SearchService searchService;

    //Use user chosen boolean operator when it doesn't equal the default
    if (!chosenOperator.equalsIgnoreCase(defaultBooleanOperator)) {
      searchService = new SearchService(chosenOperator);
      logger.debug("SUBLIMA: Use " + chosenOperator + " as boolean operator for search");
    } else {
      searchService = new SearchService(defaultBooleanOperator);
      logger.debug("SUBLIMA: Use " + defaultBooleanOperator + " as boolean operator for search");
    }

    String searchstring = searchService.buildSearchString(req.getCocoonRequest().getParameter("searchstring"));

    //Do deep search in external resources or not
    if (req.getCocoonRequest().getParameterValues("deepsearch") != null && "deepsearch".equalsIgnoreCase(req.getCocoonRequest().getParameterValues("deepsearch")[0])) {
      deepsearch = true;
      logger.debug("SUBLIMA: Deep search enabled");
    }


    String queryString = StringUtils.join("\n", new String[]{
            "PREFIX pf: <http://jena.hpl.hp.com/ARQ/property#>",
            "PREFIX dct: <http://purl.org/dc/terms/>",
            "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>",
            "DESCRIBE ?resource ?subject ?publisher",
            "WHERE {",
            "    ?lit pf:textMatch ( '" + searchstring + "' 100) .",
            "  {",
            "    ?resource ?p1 ?lit;",
            "              dct:subject ?subject;",
            "              dct:publisher ?publisher.",
            "  }",
            "  UNION",
            "  {",
            "      ?resource dct:subject ?subject1 .",
            "      ?subject1 ?p2 ?lit .",
            "      ?resource dct:subject ?subject;",
            "                dct:publisher ?publisher .",
            "  }",
            "}",
    });


    logger.trace("Freetext search: SPARQL query sent to dispatcher: " + queryString);

    Object queryResult = sparqlDispatcher.query(queryString);

    Map<String, Object> bizData = new HashMap<String, Object>();
    bizData.put("result-list", queryResult);
    bizData.put("navigation", "<empty></empty>");
    bizData.put("mode", mode);
    bizData.put("configuration", new Object());
    bizData.put("request", "<empty></empty>");
    res.sendPage("xml/sparql-result", bizData);
  }

  public void doAdvancedSearch(AppleResponse res, AppleRequest req) {
    // Get all parameteres from the HTML form as Map
    Map<String, String[]> parameterMap = new TreeMap<String, String[]>(createParametersMap(req.getCocoonRequest()));


    if ("resource".equalsIgnoreCase(mode)) {
      parameterMap.put("prefix", new String[]{"dct: <http://purl.org/dc/terms/>", "rdfs: <http://www.w3.org/2000/01/rdf-schema#>"});
      parameterMap.put("interface-language", new String[]{req.getSitemapParameter("interface-language")});
      parameterMap.put("dct:identifier", new String[]{"http://sublima.computas.com/resource/"
              + req.getSitemapParameter("name")});
      parameterMap.put("dct:subject/rdfs:label", new String[]{""});
    }

    // sending the result
    String sparqlQuery = null;
    // Check for magic prefixes
    if (parameterMap.get("prefix") != null) {
      // Calls the Form2SPARQL service with the parameterMap which returns
      // a SPARQL as String
      Form2SparqlService form2SparqlService = new Form2SparqlService(parameterMap.get("prefix"));
      parameterMap.remove("prefix"); // The prefixes are magic variables
      sparqlQuery = form2SparqlService.convertForm2Sparql(parameterMap);
    } else {
      res.sendStatus(400);
    }
    
    logger.trace("SPARQL query sent to dispatcher: " + sparqlQuery);
    Object queryResult = sparqlDispatcher.query(sparqlQuery);

    Map<String, Object> bizData = new HashMap<String, Object>();
    bizData.put("result-list", queryResult);
    bizData.put("navigation", "<empty></empty>");
    bizData.put("mode", mode);

    // This is such a 1999 way of doing things. There should be a generic SAX events generator 
    // or something that would serialise this data structure automatically in a one-liner, 
    // but I couldn't find it. Arguably a TODO.
    StringBuffer params = new StringBuffer();
    params.append("  <request>\n");
    for (Enumeration keys = req.getCocoonRequest().getParameterNames(); keys.hasMoreElements();) {
      String key = keys.nextElement().toString();
      params.append("\n    <param key=\"" + key + "\">");
      String[] values = req.getCocoonRequest().getParameterValues(key);
      for (String value : values) {
        value = value.replaceAll("<", "&lt;");
        value = value.replaceAll(">", "&gt;");
        value = value.replaceAll("#", "%23"); // A hack to get the hash alive through a clickable URL
        params.append("\n      <value>" + value + "</value>");
      }
      params.append("\n    </param>");
    }
    params.append("\n  </request>\n");

    bizData.put("request", params.toString());
    res.sendPage("xml/sparql-result", bizData);
  }

  private Map<String, String[]> createParametersMap(Request request) {
    Map<String, String[]> result = new HashMap<String, String[]>();
    Enumeration parameterNames = request.getParameterNames();
    while (parameterNames.hasMoreElements()) {
      String paramName = (String) parameterNames.nextElement();
      result.put(paramName, request.getParameterValues(paramName));
    }
    return result;
  }

  public void setSparqlDispatcher(SparqlDispatcher sparqlDispatcher) {
    this.sparqlDispatcher = sparqlDispatcher;
  }
}
