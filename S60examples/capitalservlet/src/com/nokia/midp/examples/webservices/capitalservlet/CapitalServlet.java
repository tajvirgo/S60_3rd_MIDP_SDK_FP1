/* Copyright © 2006 Nokia. */
package com.nokia.midp.examples.webservices.capitalservlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet acting like a Web Service. Publishes one Web Service method.
 */
public class CapitalServlet
    extends HttpServlet {

  /** The nations and capitals */
  private Properties capitals_;

  /**
   * Loads the nations and capitals from resource file to memory.
   */
  public void init(ServletConfig config) throws ServletException {
    capitals_ = new Properties();
    try {
      capitals_.load(config.getServletContext().getResourceAsStream(
          "/res/Nation_Capital.txt"));
    }
    catch (Exception e) {
      throw new ServletException(e);
    }
  }

  /**
   * Web Service is accessed using POST. A nation's name is searched for in
   * the request. Servlet responds with a manually generated SOAP message that
   * tells the nation's capital.
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws
      IOException, ServletException {

    String capital = null;
    ServletInputStream input = request.getInputStream();
    String nation = getNation(input);
    if (nation == null) {
      capital = "Invalid call to web service";
    }
    else {
      capital = capitals_.getProperty(nation);
      if (capital == null) {
        capital = "Capital not found";
      }
    }

    /** Manual output for the response */
    response.setContentType("text/xml");
    response.setCharacterEncoding("UTF-8");
    PrintWriter out = response.getWriter();
    out.print("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
    out.print("<soap:Envelope");
    out.print("  xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"");
    out.print("  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"");
    out.print("  xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">");
    out.print("  <soap:Body>");
    out.print("    <response xmlns=\"urn:nokia:midp:examples:webservices:capitals:2005-01/types\">");
    out.print(capital);
    out.print("</response>");
    out.print("  </soap:Body>");
    out.print("</soap:Envelope>");
  }

  /**
   * This method parses the SOAP formatted request contents looking for a
   * nation.
   *
   * @param input
   *            SOAP formatted POST input stream
   * @return nation found inside 'request' tags.
   */
  protected String getNation(InputStream input) {
    try {
      int value;
      StringBuffer sb = new StringBuffer();
      while ( (value = input.read()) != -1) {
        sb.append( (char) value);
      }
      // Request contains the received SOAP message
      String request = sb.toString();
      int requestIndex = request.indexOf("request");
      int nationStartIndex = request.indexOf(">", requestIndex
                                             + "request".length()) + 1;
      int nationEndIndex = request.indexOf("<", nationStartIndex);

      // Nation is found between <request> tags
      String nation = request.substring(nationStartIndex, nationEndIndex);
      return nation.trim();
    }
    catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }
}