/*
 * Copyright (C) 2008,2014 Attribyte, LLC  All Rights Reserved.
 * 
 * This software is the confidential and proprietary information of Attribyte, LLC.
 * ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Attribyte, LLC
 * 
 * ATTRIBYTE, LLC MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. ATTRIBYTE, LLC SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 */

package org.attribyte.api.http.util;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.CharStreams;
import com.google.protobuf.ByteString;
import org.attribyte.api.Logger;
import org.attribyte.api.http.Client;
import org.attribyte.api.http.GetRequestBuilder;
import org.attribyte.api.http.Request;
import org.attribyte.api.http.Response;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A parsed {@code robots.txt} file.
 */
public class RobotsTxt {

   /**
    * Creates a robots.txt from the standard location ({@code /robots.txt}).
    * @param host The hostname. The URL will be created as {@code [host]/robots.txt}.
    * @param httpClient The HTTP client for making the request.
    * @param userAgent The {@code User-Agent} sent with the request.
    * @param preserveAgents The set of agents to preserve. Agents not contained
    * in this set will be ignored during parse.
    * @param logger A logger for errors. May be {@code null}. If specified HTTP errors during
    * parse will be logged at the {@code warn} level.
    * @return The parsed robots.txt.
    */
   public static RobotsTxt parse(final String host, final Client httpClient,
                                 final String userAgent, final Set<String> preserveAgents,
                                 final Logger logger) {

      String url = host + "/robots.txt";
      if(!url.startsWith("http://")) {
         url = "http://" + url;
      }

      try {

         Request request = new GetRequestBuilder(url).addHeader("User-Agent", userAgent).create();
         Response response = httpClient.send(request);
         int responseCode = response.getStatusCode();

         if(responseCode == 200) {
            ByteString body = response.getBody();
            if(body == null) {
               return NO_ROBOTS;
            } else {
               byte[] bodyBytes = body.toByteArray();
               if(bodyBytes.length > 0) {
                  return new RobotsTxt(
                          new InputStreamReader(new ByteArrayInputStream(bodyBytes),
                                  Charsets.UTF_8
                          ), preserveAgents);
               } else {
                  return NO_ROBOTS;
               }
            }

         } else {
            return NO_ROBOTS;
         }

      } catch(IOException ioe) {
         ioe.printStackTrace();
         if(logger != null) {
            logger.warn("I/O error during parse of " + url, ioe);
         }
         return NO_ROBOTS;
      } catch(Throwable t) {
         t.printStackTrace();
         if(logger != null) {
            logger.warn("Error during parse of " + url, t);
         }
         return NO_ROBOTS;
      }
   }

   private RobotsTxt() {
      //Internal-use only...
   }

   /**
    * Parse robots.txt from a character stream.
    * @param r A reader from which the {@code robots.txt} is read.
    * @param agents A list of user agents that, if listed in the file, should be preserved.
    * The wildcard (*) is always preserved.
    */
   @SuppressWarnings("unchecked")
   public RobotsTxt(final Reader r, final Set<String> agents) throws IOException {

      Set<String> preserveAgents = null;
      if(agents != null) {
         preserveAgents = Sets.newHashSetWithExpectedSize(agents.size() + 1);
         preserveAgents.add("*");
         for(String agent : agents) {
            preserveAgents.add(agent.toLowerCase().trim());
         }
      }

      List<String>[] currRecordLists = null;
      boolean newAgent = false;
      List<String> lines = CharStreams.readLines(r);
      for(String currLine : lines) {

         currLine = currLine.trim();
         if(currLine.length() == 0 || currLine.startsWith("#")) {
            continue;
         }

         currLine = currLine.toLowerCase();

         if(currLine.startsWith("user-agent")) {
            String currAgent = getValue(currLine);
            if(currAgent != null) {
               if(currRecordLists == null || newAgent) {
                  currRecordLists = (ArrayList<String>[])Array.newInstance(String.class, 2);
                  newAgent = false;
               }

               if(preserveAgents == null) {
                  agentMap.put(currAgent, currRecordLists);
               } else if(preserveAgents.contains(currAgent)) {
                  agentMap.put(currAgent, currRecordLists);
               }
            }

         } else if(currLine.startsWith("disallow")) {
            newAgent = true;
            String path = getValue(currLine);
            if(path == null || path.length() == 0) {
               path = EMPTY_PATH;

            }
            if(currRecordLists != null) { //Agent must have appeared first
               if(currRecordLists[DISALLOW] == null) {
                  currRecordLists[DISALLOW] = Lists.newArrayListWithExpectedSize(8);
               }
               currRecordLists[DISALLOW].add(path);
            }

         } else if(currLine.startsWith("allow")) {
            newAgent = true;
            String path = getValue(currLine);
            if(path == null || path.length() == 0) {
               path = EMPTY_PATH;
            }
            if(currRecordLists != null) {
               if(currRecordLists[ALLOW] == null) {
                  currRecordLists[ALLOW] = Lists.newArrayListWithExpectedSize(8);
               }

               currRecordLists[ALLOW].add(path);
            }

         } else {
            newAgent = true;
            //Ignore
         }
      }
   }

   /**
    * Determine if a user agent is allowed for the specified path.
    * @param userAgent The user agent string.
    * @param path The path.
    * @return Is the agent allowed?
    */
   public final boolean isAllowed(String userAgent, String path) {
      return isAllowed(userAgent, path, true);
   }

   /**
    * Determine if a user agent is allowed for the specified path.
    * <p>
    * Technically, the treatment of Allow is not right (http://www.robotstxt.org/wc/norobots-rfc.html).
    * A single list should be processed - matching all records in the order they appear.  However,
    * in practice, I have found that many times people do things that don't make sense - like disallow all,
    * then allow, etc.
    * </p>
    * @param userAgent The user agent string.
    * @param path The path.
    * @param checkWildcard Should the wildcard record be checked? (This gives a way to know if a
    * user agent is explicitly disallowed by name.)
    * @return Is the agent allowed?
    */
   public final boolean isAllowed(String userAgent, String path, final boolean checkWildcard) {

      path = path == null ? "/" : path.toLowerCase().trim();
      if(path.length() == 0)
         path = "/";

      userAgent = userAgent.toLowerCase().trim();

      List<String>[] agentLists = agentMap.get(userAgent);
      if(agentLists == null && checkWildcard) {
         agentLists = agentMap.get("*");
      }

      if(agentLists == null) //Empty, or no wildcard...
         return true;

      List<String> allowList = agentLists[ALLOW];
      if(allowList != null) {
         for(String matchPath : allowList) {
            if(matchPath == EMPTY_PATH) {  //Allow none
               return false;
            }
            if(path.startsWith(matchPath)) { //Explicitly allowed
               return true;
            }
         }
      }

      List<String> disallowList = agentLists[DISALLOW];
      if(disallowList == null && allowList != null) {
         return false; //If allows are specified - assume these are the only things allowed
      } else if(disallowList == null) {
         return true;
      }

      for(String matchPath : disallowList) {
         if(matchPath == EMPTY_PATH) {
            return true; //Disallow none
         }
         if(path.startsWith(matchPath)) {
            return false;
         }
      }

      return true;
   }

   private String getValue(final String currLine) {
      int index = currLine.indexOf(":");
      if(index == -1)
         return null;
      if(index == currLine.length() - 1)
         return null; //No value at end of line

      int endIndex = currLine.indexOf("#"); //EOL comment
      if(endIndex > 0) {
         return currLine.substring(index + 1, endIndex).trim();
      } else {
         return currLine.substring(index + 1).trim();
      }
   }

   private final Map<String, List<String>[]> agentMap = Maps.newHashMapWithExpectedSize(8);
   private static final int DISALLOW = 1;
   private static final int ALLOW = 0;
   private static final String EMPTY_PATH = "";
   public static final RobotsTxt NO_ROBOTS = new RobotsTxt();
}