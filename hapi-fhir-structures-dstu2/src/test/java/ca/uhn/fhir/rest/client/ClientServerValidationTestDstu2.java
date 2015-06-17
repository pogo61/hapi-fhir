package ca.uhn.fhir.rest.client;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicStatusLine;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.internal.stubbing.defaultanswers.ReturnsDeepStubs;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.dstu2.resource.Conformance;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.rest.client.exceptions.FhirClientConnectionException;
import ca.uhn.fhir.rest.client.interceptor.BasicAuthInterceptor;
import ca.uhn.fhir.rest.server.Constants;

public class ClientServerValidationTestDstu2 {

	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(ClientServerValidationTestDstu2.class);
	private FhirContext myCtx;
	private HttpClient myHttpClient;
	private HttpResponse myHttpResponse;
	private boolean myFirstResponse;

	@Before
	public void before() {
		myHttpClient = mock(HttpClient.class, new ReturnsDeepStubs());
		myHttpResponse = mock(HttpResponse.class, new ReturnsDeepStubs());

		myCtx = FhirContext.forDstu2();
		myCtx.getRestfulClientFactory().setHttpClient(myHttpClient);
		myFirstResponse = true;
	}

	@Test
	public void testServerReturnsAppropriateVersionForDstu2_040() throws Exception {
		Conformance conf = new Conformance();
		conf.setFhirVersion("0.5.0");
		final String confResource = myCtx.newXmlParser().encodeResourceToString(conf);

		ArgumentCaptor<HttpUriRequest> capt = ArgumentCaptor.forClass(HttpUriRequest.class);

		when(myHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
		when(myHttpResponse.getEntity().getContentType()).thenReturn(new BasicHeader("content-type", Constants.CT_FHIR_XML + "; charset=UTF-8"));
		when(myHttpResponse.getEntity().getContent()).thenAnswer(new Answer<InputStream>() {
			@Override
			public InputStream answer(InvocationOnMock theInvocation) throws Throwable {
				if (myFirstResponse) {
					myFirstResponse = false;
					return new ReaderInputStream(new StringReader(confResource), Charset.forName("UTF-8"));
				} else {
					return new ReaderInputStream(new StringReader(myCtx.newXmlParser().encodeResourceToString(new Patient())), Charset.forName("UTF-8"));
				}
			}
		});

		when(myHttpClient.execute(capt.capture())).thenReturn(myHttpResponse);

		myCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.ONCE);
		IGenericClient client = myCtx.newRestfulGenericClient("http://foo");

		// don't load the conformance until the first time the client is actually used
		assertTrue(myFirstResponse);
		client.read(new UriDt("http://foo/Patient/123"));
		assertFalse(myFirstResponse);
		myCtx.newRestfulGenericClient("http://foo").read(new UriDt("http://foo/Patient/123"));
		myCtx.newRestfulGenericClient("http://foo").read(new UriDt("http://foo/Patient/123"));

		// Conformance only loaded once, then 3 reads
		verify(myHttpClient, times(4)).execute(Matchers.any(HttpUriRequest.class));
	}

	@Test
	public void testServerReturnsAppropriateVersionForDstu2_050() throws Exception {
		Conformance conf = new Conformance();
		conf.setFhirVersion("0.5.0");
		final String confResource = myCtx.newXmlParser().encodeResourceToString(conf);

		ArgumentCaptor<HttpUriRequest> capt = ArgumentCaptor.forClass(HttpUriRequest.class);

		when(myHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
		when(myHttpResponse.getEntity().getContentType()).thenReturn(new BasicHeader("content-type", Constants.CT_FHIR_XML + "; charset=UTF-8"));
		when(myHttpResponse.getEntity().getContent()).thenAnswer(new Answer<InputStream>() {
			@Override
			public InputStream answer(InvocationOnMock theInvocation) throws Throwable {
				if (myFirstResponse) {
					myFirstResponse = false;
					return new ReaderInputStream(new StringReader(confResource), Charset.forName("UTF-8"));
				} else {
					return new ReaderInputStream(new StringReader(myCtx.newXmlParser().encodeResourceToString(new Patient())), Charset.forName("UTF-8"));
				}
			}
		});

		when(myHttpClient.execute(capt.capture())).thenReturn(myHttpResponse);

		myCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.ONCE);
		IGenericClient client = myCtx.newRestfulGenericClient("http://foo");

		// don't load the conformance until the first time the client is actually used
		assertTrue(myFirstResponse);
		client.read(new UriDt("http://foo/Patient/123"));
		assertFalse(myFirstResponse);
		myCtx.newRestfulGenericClient("http://foo").read(new UriDt("http://foo/Patient/123"));
		myCtx.newRestfulGenericClient("http://foo").read(new UriDt("http://foo/Patient/123"));

		// Conformance only loaded once, then 3 reads
		verify(myHttpClient, times(4)).execute(Matchers.any(HttpUriRequest.class));
	}

	@Test
	public void testServerReturnsWrongVersionForDstu2() throws Exception {
		Conformance conf = new Conformance();
		conf.setFhirVersion("0.80");
		String msg = myCtx.newXmlParser().encodeResourceToString(conf);

		ArgumentCaptor<HttpUriRequest> capt = ArgumentCaptor.forClass(HttpUriRequest.class);

		when(myHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
		when(myHttpResponse.getEntity().getContentType()).thenReturn(new BasicHeader("content-type", Constants.CT_FHIR_XML + "; charset=UTF-8"));
		when(myHttpResponse.getEntity().getContent()).thenReturn(new ReaderInputStream(new StringReader(msg), Charset.forName("UTF-8")));

		when(myHttpClient.execute(capt.capture())).thenReturn(myHttpResponse);

		myCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.ONCE);
		try {
			myCtx.newRestfulGenericClient("http://foo").read(new UriDt("http://foo/Patient/123"));
			fail();
		} catch (FhirClientConnectionException e) {
			String out = e.toString();
			String want = "The server at base URL \"http://foo/metadata\" returned a conformance statement indicating that it supports FHIR version \"0.80\" which corresponds to DSTU1, but this client is configured to use DSTU2 (via the FhirContext)";
			ourLog.info(out);
			ourLog.info(want);
			assertThat(out, containsString(want));
		}
	}

	@Test
	public void testClientUsesInterceptors() throws Exception {
		Conformance conf = new Conformance();
		conf.setFhirVersion("0.5.0");
		final String confResource = myCtx.newXmlParser().encodeResourceToString(conf);

		ArgumentCaptor<HttpUriRequest> capt = ArgumentCaptor.forClass(HttpUriRequest.class);

		when(myHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
		when(myHttpResponse.getEntity().getContentType()).thenReturn(new BasicHeader("content-type", Constants.CT_FHIR_XML + "; charset=UTF-8"));
		when(myHttpResponse.getEntity().getContent()).thenAnswer(new Answer<InputStream>() {
			@Override
			public InputStream answer(InvocationOnMock theInvocation) throws Throwable {
				if (myFirstResponse) {
					myFirstResponse = false;
					return new ReaderInputStream(new StringReader(confResource), Charset.forName("UTF-8"));
				} else {
					Patient resource = new Patient();
					resource.addName().addFamily().setValue("FAM");
					return new ReaderInputStream(new StringReader(myCtx.newXmlParser().encodeResourceToString(resource)), Charset.forName("UTF-8"));
				}
			}
		});

		when(myHttpClient.execute(capt.capture())).thenReturn(myHttpResponse);

		myCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.ONCE);
		IGenericClient client = myCtx.newRestfulGenericClient("http://foo");
		client.registerInterceptor(new BasicAuthInterceptor("USER", "PASS"));
		Patient pt = (Patient) client.read(new UriDt("http://foo/Patient/123"));
		assertEquals("FAM", pt.getNameFirstRep().getFamilyAsSingleString());
		
		assertEquals(2, capt.getAllValues().size());
		
		Header auth = capt.getAllValues().get(0).getFirstHeader("Authorization");
		assertNotNull(auth);
		assertEquals("Basic VVNFUjpQQVNT", auth.getValue());
		auth = capt.getAllValues().get(1).getFirstHeader("Authorization");
		assertNotNull(auth);
		assertEquals("Basic VVNFUjpQQVNT", auth.getValue());
	}

	@Test
	public void testForceConformanceCheck() throws Exception {
		Conformance conf = new Conformance();
		conf.setFhirVersion("0.5.0");
		final String confResource = myCtx.newXmlParser().encodeResourceToString(conf);

		ArgumentCaptor<HttpUriRequest> capt = ArgumentCaptor.forClass(HttpUriRequest.class);

		when(myHttpResponse.getStatusLine()).thenReturn(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1), 200, "OK"));
		when(myHttpResponse.getEntity().getContentType()).thenReturn(new BasicHeader("content-type", Constants.CT_FHIR_XML + "; charset=UTF-8"));
		when(myHttpResponse.getEntity().getContent()).thenAnswer(new Answer<InputStream>() {
			@Override
			public InputStream answer(InvocationOnMock theInvocation) throws Throwable {
				if (myFirstResponse) {
					myFirstResponse = false;
					return new ReaderInputStream(new StringReader(confResource), Charset.forName("UTF-8"));
				} else {
					Patient resource = new Patient();
					resource.addName().addFamily().setValue("FAM");
					return new ReaderInputStream(new StringReader(myCtx.newXmlParser().encodeResourceToString(resource)), Charset.forName("UTF-8"));
				}
			}
		});

		when(myHttpClient.execute(capt.capture())).thenReturn(myHttpResponse);

		myCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.ONCE);
		
		IGenericClient client = myCtx.newRestfulGenericClient("http://foo");
		client.registerInterceptor(new BasicAuthInterceptor("USER", "PASS"));
		
		client.forceConformanceCheck();

		assertEquals(1, capt.getAllValues().size());

		Patient pt = (Patient) client.read(new UriDt("http://foo/Patient/123"));
		assertEquals("FAM", pt.getNameFirstRep().getFamilyAsSingleString());
		
		assertEquals(2, capt.getAllValues().size());
		
		Header auth = capt.getAllValues().get(0).getFirstHeader("Authorization");
		assertNotNull(auth);
		assertEquals("Basic VVNFUjpQQVNT", auth.getValue());
		auth = capt.getAllValues().get(1).getFirstHeader("Authorization");
		assertNotNull(auth);
		assertEquals("Basic VVNFUjpQQVNT", auth.getValue());
	}

}
