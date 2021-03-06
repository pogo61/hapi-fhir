package ca.uhn.fhir.rest.server.interceptor;

/*
 * #%L
 * HAPI FHIR - Core Library
 * %%
 * Copyright (C) 2014 - 2015 University Health Network
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.uhn.fhir.rest.method.RequestDetails;
import ca.uhn.fhir.rest.server.EncodingEnum;
import ca.uhn.fhir.rest.server.RestfulServerUtils;
import ca.uhn.fhir.rest.server.exceptions.AuthenticationException;

/**
 * Server interceptor which logs each request using a defined format
 * <p>
 * The following substitution variables are supported:
 * </p>
 * <table summary="Substitution variables supported by this class">
 * <tr>
 * <td>${id}</td>
 * <td>The resource ID associated with this request (or "" if none)</td>
 * </tr>
 * <tr>
 * <td>${idOrResourceName}</td>
 * <td>The resource ID associated with this request, or the resource name if the request applies to a type but not an instance, or "" otherwise</td>
 * </tr>
 * <tr>
 * <td>${operationName}</td>
 * <td>If the request is an extended operation (e.g. "$validate") this value will be the operation name, or "" otherwise</td>
 * </tr>
 * <tr>
 * <td>${operationType}</td>
 * <td>A code indicating the operation type for this request, e.g. "read", "history-instance", "extended-operation-instance", etc.)</td>
 * </tr>
 * <tr>
 * <td>${remoteAddr}</td>
 * <td>The originaring IP of the request</td>
 * </tr>
 * <tr>
 * <td>${requestHeader.XXXX}</td>
 * <td>The value of the HTTP request header named XXXX. For example, a substitution variable named
 * "${requestHeader.x-forwarded-for} will yield the value of the first header named "x-forwarded-for", or "" if none.</td>
 * </tr>
 * <tr>
 * <td>${requestParameters}</td>
 * <td>The HTTP request parameters (or "")</td>
 * </tr>
 * <tr>
 * <td>${responseEncodingNoDefault}</td>
 * <td>The encoding format requested by the client via the _format parameter or the Accept header. Value will be "json" or "xml", or "" if the client did not explicitly request a format</td>
 * </tr>
 * <tr>
 * <td>${servletPath}</td>
 * <td>The part of thre requesting URL that corresponds to the particular Servlet being called (see {@link HttpServletRequest#getServletPath()})</td>
 * </tr>
 * </table>
 */
public class LoggingInterceptor extends InterceptorAdapter {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(LoggingInterceptor.class);

	private Logger myLogger = ourLog;
	private String myMessageFormat = "${operationType} - ${idOrResourceName}";

	@Override
	public boolean incomingRequestPostProcessed(final RequestDetails theRequestDetails, final HttpServletRequest theRequest, HttpServletResponse theResponse) throws AuthenticationException {

		// Perform any string substitutions from the message format
		StrLookup<?> lookup = new MyLookup(theRequest, theRequestDetails);
		StrSubstitutor subs = new StrSubstitutor(lookup, "${", "}", '\\');

		// Actuall log the line
		String line = subs.replace(myMessageFormat);
		myLogger.info(line);

		return true;
	}

	public void setLogger(Logger theLogger) {
		Validate.notNull(theLogger, "Logger can not be null");
		myLogger = theLogger;
	}

	public void setLoggerName(String theLoggerName) {
		Validate.notBlank(theLoggerName, "Logger name can not be null/empty");
		myLogger = LoggerFactory.getLogger(theLoggerName);

	}

	/**
	 * Sets the message format itself. See the {@link LoggingInterceptor class documentation} for information on the format
	 */
	public void setMessageFormat(String theMessageFormat) {
		Validate.notBlank(theMessageFormat, "Message format can not be null/empty");
		myMessageFormat = theMessageFormat;
	}

	private static final class MyLookup extends StrLookup<String> {
		private final HttpServletRequest myRequest;
		private final RequestDetails myRequestDetails;

		private MyLookup(HttpServletRequest theRequest, RequestDetails theRequestDetails) {
			myRequest = theRequest;
			myRequestDetails = theRequestDetails;
		}

		@Override
		public String lookup(String theKey) {

			/*
			 * TODO: this method could be made more efficient through some sort of lookup map
			 */

			if ("operationType".equals(theKey)) {
				if (myRequestDetails.getResourceOperationType() != null) {
					return myRequestDetails.getResourceOperationType().getCode();
				}
				if (myRequestDetails.getSystemOperationType() != null) {
					return myRequestDetails.getSystemOperationType().getCode();
				}
				if (myRequestDetails.getOtherOperationType() != null) {
					return myRequestDetails.getOtherOperationType().getCode();
				}
				return "";
			} else if ("operationName".equals(theKey)) {
				if (myRequestDetails.getOtherOperationType() != null) {
					switch (myRequestDetails.getOtherOperationType()) {
					case EXTENDED_OPERATION_INSTANCE:
					case EXTENDED_OPERATION_SERVER:
					case EXTENDED_OPERATION_TYPE:
						return myRequestDetails.getOperation();
					default:
						return "";
					}
				} else {
					return "";
				}
			} else if ("id".equals(theKey)) {
				if (myRequestDetails.getId() != null) {
					return myRequestDetails.getId().getValue();
				}
				return "";
			} else if ("servletPath".equals(theKey)) {
				return StringUtils.defaultString(myRequest.getServletPath());
			} else if ("idOrResourceName".equals(theKey)) {
				if (myRequestDetails.getId() != null) {
					return myRequestDetails.getId().getValue();
				}
				if (myRequestDetails.getResourceName() != null) {
					return myRequestDetails.getResourceName();
				}
				return "";
			} else if (theKey.equals("requestParameters")) {
				StringBuilder b = new StringBuilder();
				for (Entry<String, String[]> next : myRequestDetails.getParameters().entrySet()) {
					for (String nextValue : next.getValue()) {
						if (b.length() == 0) {
							b.append('?');
						} else {
							b.append('&');
						}
						try {
							b.append(URLEncoder.encode(next.getKey(), "UTF-8"));
							b.append('=');
							b.append(URLEncoder.encode(nextValue, "UTF-8"));
						} catch (UnsupportedEncodingException e) {
							throw new ca.uhn.fhir.context.ConfigurationException("UTF-8 not supported", e);
						}
					}
				}
				return b.toString();
			} else if (theKey.startsWith("requestHeader.")) {
				String val = myRequest.getHeader(theKey.substring("requestHeader.".length()));
				return StringUtils.defaultString(val);
			} else if (theKey.startsWith("remoteAddr")) {
				return StringUtils.defaultString(myRequest.getRemoteAddr());
			} else if (theKey.equals("responseEncodingNoDefault")) {
				EncodingEnum encoding = RestfulServerUtils.determineResponseEncodingNoDefault(myRequest);
				if (encoding != null) {
					return encoding.name();
				} else {
					return "";
				}
			}

			return "!VAL!";
		}
	}

}
