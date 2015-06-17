package ca.uhn.fhir.rest.server;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.net.URLEncoder;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.dstu.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu.resource.Organization;
import ca.uhn.fhir.model.dstu.resource.Patient;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.util.PortUtil;

/**
 * Created by dsotnikov on 2/25/2014.
 */
public class AcceptHeaderTest {

	private static CloseableHttpClient ourClient;
	private static int ourPort;
	private static Server ourServer;

	@Test
	public void testReadNoHeader() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient/1");
		HttpResponse status = ourClient.execute(httpGet);
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertEquals(Constants.CT_FHIR_XML + Constants.CTSUFFIX_CHARSET_UTF8, status.getFirstHeader(Constants.HEADER_CONTENT_TYPE).getValue());
	}
	
	@Test
	public void testReadXmlHeaderHigherPriority() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient/1");
		httpGet.addHeader(Constants.HEADER_ACCEPT, Constants.CT_FHIR_XML + "; q=1.0, " + Constants.CT_FHIR_JSON + "; q=0.9");
		HttpResponse status = ourClient.execute(httpGet);
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertEquals(Constants.CT_FHIR_XML + Constants.CTSUFFIX_CHARSET_UTF8, status.getFirstHeader(Constants.HEADER_CONTENT_TYPE).getValue());
	}

	@Test
	public void testReadXmlHeaderHigherPriorityWithStar() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient/1");
		httpGet.addHeader(Constants.HEADER_ACCEPT, "*/*; q=1.0, " + Constants.CT_FHIR_XML + "; q=1.0, " + Constants.CT_FHIR_JSON + "; q=0.9");
		HttpResponse status = ourClient.execute(httpGet);
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertEquals(Constants.CT_FHIR_XML + Constants.CTSUFFIX_CHARSET_UTF8, status.getFirstHeader(Constants.HEADER_CONTENT_TYPE).getValue());
	}

	@Test
	public void testReadXmlHeaderHigherPriorityBackwards() throws Exception {
		HttpGet httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient/1");
		httpGet.addHeader(Constants.HEADER_ACCEPT, Constants.CT_FHIR_JSON + "; q=0.9, " + Constants.CT_FHIR_XML + "; q=1.0");
		HttpResponse status = ourClient.execute(httpGet);
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertEquals(Constants.CT_FHIR_XML + Constants.CTSUFFIX_CHARSET_UTF8, status.getFirstHeader(Constants.HEADER_CONTENT_TYPE).getValue());

		// Now with spaces
		httpGet = new HttpGet("http://localhost:" + ourPort + "/Patient/1");
		httpGet.addHeader(Constants.HEADER_ACCEPT, Constants.CT_FHIR_JSON + "; q   =        000000.9     , " + Constants.CT_FHIR_XML + "    ;     q    =    1.0  ,");
		status = ourClient.execute(httpGet);
		IOUtils.closeQuietly(status.getEntity().getContent());

		assertEquals(Constants.CT_FHIR_XML + Constants.CTSUFFIX_CHARSET_UTF8, status.getFirstHeader(Constants.HEADER_CONTENT_TYPE).getValue());
	}

	@AfterClass
	public static void afterClass() throws Exception {
		ourServer.stop();
	}

	@BeforeClass
	public static void beforeClass() throws Exception {
		ourPort = PortUtil.findFreePort();
		ourServer = new Server(ourPort);

		ServletHandler proxyHandler = new ServletHandler();
		RestfulServer servlet = new RestfulServer();
		servlet.setResourceProviders(new PatientProvider());
		servlet.setDefaultResponseEncoding(EncodingEnum.XML);
		ServletHolder servletHolder = new ServletHolder(servlet);
		proxyHandler.addServletWithMapping(servletHolder, "/*");
		ourServer.setHandler(proxyHandler);
		ourServer.start();

		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(5000, TimeUnit.MILLISECONDS);
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setConnectionManager(connectionManager);
		ourClient = builder.build();

	}

	/**
	 * Created by dsotnikov on 2/25/2014.
	 */
	public static class PatientProvider implements IResourceProvider {

		@Read(version = true)
		public Patient read(@IdParam IdDt theId) {
			Patient patient = new Patient();
			patient.addIdentifier(theId.getIdPart(), theId.getVersionIdPart());
			patient.setId("Patient/1/_history/1");
			return patient;
		}

		@Override
		public Class<? extends IResource> getResourceType() {
			return Patient.class;
		}

	}

}
