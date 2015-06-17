package ca.uhn.fhir.jpa.dao;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsInRelativeOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.hamcrest.core.StringContains;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.entity.ResourceIndexedSearchParamString;
import ca.uhn.fhir.jpa.entity.TagTypeEnum;
import ca.uhn.fhir.model.api.IQueryParameterType;
import ca.uhn.fhir.model.api.IResource;
import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.model.api.ResourceMetadataKeyEnum;
import ca.uhn.fhir.model.api.TagList;
import ca.uhn.fhir.model.base.composite.BaseCodingDt;
import ca.uhn.fhir.model.dstu.valueset.QuantityCompararatorEnum;
import ca.uhn.fhir.model.dstu2.composite.CodeableConceptDt;
import ca.uhn.fhir.model.dstu2.composite.CodingDt;
import ca.uhn.fhir.model.dstu2.composite.IdentifierDt;
import ca.uhn.fhir.model.dstu2.composite.MetaDt;
import ca.uhn.fhir.model.dstu2.composite.PeriodDt;
import ca.uhn.fhir.model.dstu2.composite.QuantityDt;
import ca.uhn.fhir.model.dstu2.composite.ResourceReferenceDt;
import ca.uhn.fhir.model.dstu2.resource.Bundle;
import ca.uhn.fhir.model.dstu2.resource.Device;
import ca.uhn.fhir.model.dstu2.resource.DiagnosticReport;
import ca.uhn.fhir.model.dstu2.resource.Encounter;
import ca.uhn.fhir.model.dstu2.resource.Location;
import ca.uhn.fhir.model.dstu2.resource.Observation;
import ca.uhn.fhir.model.dstu2.resource.Organization;
import ca.uhn.fhir.model.dstu2.resource.Patient;
import ca.uhn.fhir.model.dstu2.valueset.AdministrativeGenderEnum;
import ca.uhn.fhir.model.dstu2.valueset.HTTPVerbEnum;
import ca.uhn.fhir.model.dstu2.valueset.QuantityComparatorEnum;
import ca.uhn.fhir.model.primitive.DateDt;
import ca.uhn.fhir.model.primitive.DateTimeDt;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.model.primitive.InstantDt;
import ca.uhn.fhir.model.primitive.StringDt;
import ca.uhn.fhir.model.primitive.UriDt;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.api.SortOrderEnum;
import ca.uhn.fhir.rest.api.SortSpec;
import ca.uhn.fhir.rest.param.CompositeParam;
import ca.uhn.fhir.rest.param.DateParam;
import ca.uhn.fhir.rest.param.DateRangeParam;
import ca.uhn.fhir.rest.param.NumberParam;
import ca.uhn.fhir.rest.param.QuantityParam;
import ca.uhn.fhir.rest.param.ReferenceParam;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.param.TokenOrListParam;
import ca.uhn.fhir.rest.param.TokenParam;
import ca.uhn.fhir.rest.server.IBundleProvider;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.PreconditionFailedException;
import ca.uhn.fhir.rest.server.exceptions.ResourceGoneException;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;

@SuppressWarnings("unchecked")
public class FhirResourceDaoDstu2Test {

	private static ClassPathXmlApplicationContext ourCtx;
	private static IFhirResourceDao<Device> ourDeviceDao;
	private static IFhirResourceDao<DiagnosticReport> ourDiagnosticReportDao;
	private static IFhirResourceDao<Encounter> ourEncounterDao;
	private static FhirContext ourFhirCtx;
	private static IFhirResourceDao<Location> ourLocationDao;
	private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(FhirResourceDaoDstu2Test.class);
	private static IFhirResourceDao<Observation> ourObservationDao;
	private static IFhirResourceDao<Organization> ourOrganizationDao;
	private static IFhirResourceDao<Patient> ourPatientDao;
	private static IFhirSystemDao<Bundle> ourSystemDao;

	@Test
	public void testChoiceParamConcept() {
		Observation o1 = new Observation();
		o1.getCode().addCoding().setSystem("foo").setCode("testChoiceParam01");
		o1.setValue(new CodeableConceptDt("testChoiceParam01CCS", "testChoiceParam01CCV"));
		IdDt id1 = ourObservationDao.create(o1).getId();

		{
			IBundleProvider found = ourObservationDao.search(Observation.SP_VALUE_CONCEPT, new TokenParam("testChoiceParam01CCS", "testChoiceParam01CCV"));
			assertEquals(1, found.size());
			assertEquals(id1, found.getResources(0, 1).get(0).getIdElement());
		}
	}

	@Test
	public void testChoiceParamDate() {
		Observation o2 = new Observation();
		o2.getCode().addCoding().setSystem("foo").setCode("testChoiceParam02");
		o2.setValue(new PeriodDt().setStart(new DateTimeDt("2001-01-01")).setEnd(new DateTimeDt("2001-01-03")));
		IdDt id2 = ourObservationDao.create(o2).getId();

		{
			IBundleProvider found = ourObservationDao.search(Observation.SP_VALUE_DATE, new DateParam("2001-01-02"));
			assertEquals(1, found.size());
			assertEquals(id2, found.getResources(0, 1).get(0).getIdElement());
		}
	}

	@Test
	public void testChoiceParamDateAlt() {
		Observation o2 = new Observation();
		o2.getCode().addCoding().setSystem("foo").setCode("testChoiceParamDateAlt02");
		o2.setApplies(new DateTimeDt("2015-03-08T11:11:11"));
		IdDt id2 = ourObservationDao.create(o2).getId();

		{
			Set<Long> found = ourObservationDao.searchForIds(Observation.SP_DATE, new DateParam(">2001-01-02"));
			assertThat(found, hasItem(id2.getIdPartAsLong()));
		}
		{
			Set<Long> found = ourObservationDao.searchForIds(Observation.SP_DATE, new DateParam(">2016-01-02"));
			assertThat(found, not(hasItem(id2.getIdPartAsLong())));
		}
	}

	@Test
	public void testChoiceParamQuantity() {
		Observation o3 = new Observation();
		o3.getCode().addCoding().setSystem("foo").setCode("testChoiceParam03");
		o3.setValue(new QuantityDt(QuantityComparatorEnum.GREATERTHAN, 123.0, "foo", "bar").setCode("bar"));
		IdDt id3 = ourObservationDao.create(o3).getId();

		{
			IBundleProvider found = ourObservationDao.search(Observation.SP_VALUE_QUANTITY, new QuantityParam(">100", "foo", "bar"));
			assertEquals(1, found.size());
			assertEquals(id3, found.getResources(0, 1).get(0).getIdElement());
		}
		{
			IBundleProvider found = ourObservationDao.search(Observation.SP_VALUE_QUANTITY, new QuantityParam("<100", "foo", "bar"));
			assertEquals(0, found.size());
		}
		{
			IBundleProvider found = ourObservationDao.search(Observation.SP_VALUE_QUANTITY, new QuantityParam("123.0001", "foo", "bar"));
			assertEquals(1, found.size());
			assertEquals(id3, found.getResources(0, 1).get(0).getIdElement());
		}
		{
			IBundleProvider found = ourObservationDao.search(Observation.SP_VALUE_QUANTITY, new QuantityParam("~120", "foo", "bar"));
			assertEquals(1, found.size());
			assertEquals(id3, found.getResources(0, 1).get(0).getIdElement());
		}
	}

	@Test
	public void testChoiceParamString() {

		Observation o4 = new Observation();
		o4.getCode().addCoding().setSystem("foo").setCode("testChoiceParam04");
		o4.setValue(new StringDt("testChoiceParam04Str"));
		IdDt id4 = ourObservationDao.create(o4).getId();

		{
			IBundleProvider found = ourObservationDao.search(Observation.SP_VALUE_STRING, new StringParam("testChoiceParam04Str"));
			assertEquals(1, found.size());
			assertEquals(id4, found.getResources(0, 1).get(0).getIdElement());
		}
	}

	@Test
	public void testCreateNumericIdFails() {
		Patient p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue("testCreateNumericIdFails");
		p.addName().addFamily("Hello");
		p.setId("Patient/123");
		try {
			ourPatientDao.create(p);
			fail();
		} catch (InvalidRequestException e) {
			assertThat(e.getMessage(), containsString("Can not create resource with ID[123], ID must not be supplied"));
		}
	}

	@Test
	public void testCreateTextIdFails() {
		Patient p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue("testCreateTextIdFails");
		p.addName().addFamily("Hello");
		p.setId("Patient/ABC");
		try {
			ourPatientDao.create(p);
			fail();
		} catch (InvalidRequestException e) {
			assertThat(e.getMessage(), containsString("Can not create resource with ID[ABC], ID must not be supplied"));
		}
	}

	@Test
	public void testCreateWithIfNoneExistBasic() {
		String methodName = "testCreateWithIfNoneExistBasic";
		MethodOutcome results;

		Patient p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue(methodName);
		IdDt id = ourPatientDao.create(p).getId();
		ourLog.info("Created patient, got it: {}", id);

		p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue(methodName);
		p.addName().addFamily("Hello");
		results = ourPatientDao.create(p, "Patient?identifier=urn%3Asystem%7C" + methodName);
		assertEquals(id.getIdPart(), results.getId().getIdPart());
		assertFalse(results.getCreated().booleanValue());

		// Now create a second one

		p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue(methodName);
		p.addName().addFamily("Hello");
		results = ourPatientDao.create(p);
		assertNotEquals(id.getIdPart(), results.getId().getIdPart());
		assertTrue(results.getCreated().booleanValue());

		// Now try to create one with the original match URL and it should fail

		p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue(methodName);
		p.addName().addFamily("Hello");
		try {
			ourPatientDao.create(p, "Patient?identifier=urn%3Asystem%7C" + methodName);
			fail();
		} catch (PreconditionFailedException e) {
			assertThat(e.getMessage(), containsString("Failed to CREATE"));
		}

	}

	@Test
	public void testCreateWithInvalidReferenceFailsGracefully() {
		Patient patient = new Patient();
		patient.addName().addFamily("testSearchResourceLinkWithChainWithMultipleTypes01");
		patient.setManagingOrganization(new ResourceReferenceDt("Patient/99999999"));
		try {
			ourPatientDao.create(patient);
			fail();
		} catch (InvalidRequestException e) {
			assertThat(e.getMessage(), StringContains.containsString("99999 not found"));
		}

	}

	@Test
	public void testDatePeriodParamEndOnly() {
		{
			Encounter enc = new Encounter();
			enc.addIdentifier().setSystem("testDatePeriodParam").setValue("02");
			enc.getPeriod().getEndElement().setValueAsString("2001-01-02");
			ourEncounterDao.create(enc);
		}
		SearchParameterMap params;
		List<Encounter> encs;

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam(null, "2001-01-03"));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "02"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(1, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam("2001-01-01", "2001-01-03"));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "02"));
		// encs = toList(ourEncounterDao.search(params));
		// assertEquals(1, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam("2001-01-01", null));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "02"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(1, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam(null, "2001-01-01"));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "02"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(0, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam("2001-01-03", null));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "02"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(0, encs.size());

	}

	@Test
	public void testDatePeriodParamStartAndEnd() {
		{
			Encounter enc = new Encounter();
			enc.addIdentifier().setSystem("testDatePeriodParam").setValue("03");
			enc.getPeriod().getStartElement().setValueAsString("2001-01-02");
			enc.getPeriod().getEndElement().setValueAsString("2001-01-03");
			ourEncounterDao.create(enc);
		}

		SearchParameterMap params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam("2001-01-01", "2001-01-03"));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "03"));
		List<Encounter> encs = toList(ourEncounterDao.search(params));
		assertEquals(1, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam("2001-01-02", "2001-01-06"));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "03"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(1, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam("2001-01-01", null));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "03"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(1, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam(null, "2001-01-03"));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "03"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(1, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam(null, "2001-01-05"));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "03"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(1, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam(null, "2001-01-01"));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "03"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(0, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam("2001-01-05", null));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "03"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(0, encs.size());

	}

	@Test
	public void testDatePeriodParamStartOnly() {
		{
			Encounter enc = new Encounter();
			enc.addIdentifier().setSystem("testDatePeriodParam").setValue("01");
			enc.getPeriod().getStartElement().setValueAsString("2001-01-02");
			ourEncounterDao.create(enc);
		}

		SearchParameterMap params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam("2001-01-01", "2001-01-03"));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "01"));
		List<Encounter> encs = toList(ourEncounterDao.search(params));
		assertEquals(1, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam("2001-01-01", null));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "01"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(1, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam(null, "2001-01-03"));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "01"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(1, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam(null, "2001-01-01"));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "01"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(0, encs.size());

		params = new SearchParameterMap();
		params.add(Encounter.SP_DATE, new DateRangeParam("2001-01-03", null));
		params.add(Encounter.SP_IDENTIFIER, new IdentifierDt("testDatePeriodParam", "01"));
		encs = toList(ourEncounterDao.search(params));
		assertEquals(0, encs.size());

	}

	@Test
	public void testDeleteResource() {
		int initialHistory = ourPatientDao.history(null).size();

		IdDt id1;
		IdDt id2;
		IdDt id2b;
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("001");
			patient.addName().addFamily("Tester_testDeleteResource").addGiven("Joe");
			id1 = ourPatientDao.create(patient).getId();
		}
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("002");
			patient.addName().addFamily("Tester_testDeleteResource").addGiven("John");
			id2 = ourPatientDao.create(patient).getId();
		}
		{
			Patient patient = ourPatientDao.read(id2);
			patient.addIdentifier().setSystem("ZZZZZZZ").setValue("ZZZZZZZZZ");
			id2b = ourPatientDao.update(patient).getId();
		}
		ourLog.info("ID1:{}   ID2:{}   ID2b:{}", new Object[] { id1, id2, id2b });

		Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
		params.put(Patient.SP_FAMILY, new StringDt("Tester_testDeleteResource"));
		List<Patient> patients = toList(ourPatientDao.search(params));
		assertEquals(2, patients.size());

		ourPatientDao.delete(id1);

		patients = toList(ourPatientDao.search(params));
		assertEquals(1, patients.size());

		ourPatientDao.read(id1);
		try {
			ourPatientDao.read(id1.toVersionless());
			fail();
		} catch (ResourceGoneException e) {
			// good
		}

		IBundleProvider history = ourPatientDao.history(null);
		assertEquals(4 + initialHistory, history.size());
		List<IBaseResource> resources = history.getResources(0, 4);
		assertNotNull(ResourceMetadataKeyEnum.DELETED_AT.get((IResource) resources.get(0)));

		try {
			ourPatientDao.delete(id2);
			fail();
		} catch (InvalidRequestException e) {
			// good
		}

		ourPatientDao.delete(id2.toVersionless());

		patients = toList(ourPatientDao.search(params));
		assertEquals(0, patients.size());

	}

	@Test
	public void testDeleteThenUndelete() {
		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("001");
		patient.addName().addFamily("Tester_testDeleteThenUndelete").addGiven("Joe");
		IdDt id = ourPatientDao.create(patient).getId();
		assertThat(id.getValue(), endsWith("/_history/1"));

		// should be ok
		ourPatientDao.read(id.toUnqualifiedVersionless());

		// Delete it
		ourPatientDao.delete(id.toUnqualifiedVersionless());

		try {
			ourPatientDao.read(id.toUnqualifiedVersionless());
			fail();
		} catch (ResourceGoneException e) {
			// expected
		}

		patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("001");
		patient.addName().addFamily("Tester_testDeleteThenUndelete").addGiven("Joe");
		patient.setId(id.toUnqualifiedVersionless());
		IdDt id2 = ourPatientDao.update(patient).getId();

		assertThat(id2.getValue(), endsWith("/_history/3"));

		IdDt gotId = ourPatientDao.read(id.toUnqualifiedVersionless()).getId();
		assertEquals(id2, gotId);
	}

	@Test
	public void testDeleteWithMatchUrl() {
		String methodName = "testDeleteWithMatchUrl";

		Patient p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue(methodName);
		IdDt id = ourPatientDao.create(p).getId();
		ourLog.info("Created patient, got it: {}", id);

		Bundle request = new Bundle();
		request.addEntry().setResource(p).getTransaction().setMethod(HTTPVerbEnum.DELETE).setUrl("Patient?identifier=urn%3Asystem%7C" + methodName);

		ourPatientDao.deleteByUrl("Patient?identifier=urn%3Asystem%7C" + methodName);

		try {
			ourPatientDao.read(id.toVersionless());
			fail();
		} catch (ResourceGoneException e) {
			// ok
		}

		try {
			ourPatientDao.read(new IdDt("Patient/" + methodName));
			fail();
		} catch (ResourceNotFoundException e) {
			// ok
		}

		IBundleProvider history = ourPatientDao.history(id, null);
		assertEquals(2, history.size());

		assertNotNull(ResourceMetadataKeyEnum.DELETED_AT.get((IResource) history.getResources(0, 0).get(0)));
		assertNotNull(ResourceMetadataKeyEnum.DELETED_AT.get((IResource) history.getResources(0, 0).get(0)).getValue());
		assertNull(ResourceMetadataKeyEnum.DELETED_AT.get((IResource) history.getResources(1, 1).get(0)));

	}

	@Test
	public void testHistoryByForcedId() {
		IdDt idv1;
		IdDt idv2;
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("testHistoryByForcedId");
			patient.addName().addFamily("Tester").addGiven("testHistoryByForcedId");
			patient.setId("Patient/testHistoryByForcedId");
			idv1 = ourPatientDao.update(patient).getId();

			patient.addName().addFamily("Tester").addGiven("testHistoryByForcedIdName2");
			patient.setId(patient.getId().toUnqualifiedVersionless());
			idv2 = ourPatientDao.update(patient).getId();
		}

		List<Patient> patients = toList(ourPatientDao.history(idv1.toVersionless(), null));
		assertTrue(patients.size() == 2);
		// Newest first
		assertEquals("Patient/testHistoryByForcedId/_history/2", patients.get(0).getId().toUnqualified().getValue());
		assertEquals("Patient/testHistoryByForcedId/_history/1", patients.get(1).getId().toUnqualified().getValue());
		assertNotEquals(idv1, idv2);
	}

	@Test
	public void testIdParam() {
		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("001");
		patient.addName().addFamily("Tester").addGiven("Joe");

		MethodOutcome outcome = ourPatientDao.create(patient);
		assertNotNull(outcome.getId());
		assertFalse(outcome.getId().isEmpty());

		Date now = new Date();

		{
			Patient retrieved = ourPatientDao.read(outcome.getId());
			InstantDt published = (InstantDt) retrieved.getResourceMetadata().get(ResourceMetadataKeyEnum.PUBLISHED);
			InstantDt updated = (InstantDt) retrieved.getResourceMetadata().get(ResourceMetadataKeyEnum.UPDATED);
			assertTrue(published.before(now));
			assertTrue(updated.before(now));
		}

		/*
		 * This ID points to a patient, so we should not be able to return othe types with it
		 */
		try {
			ourEncounterDao.read(outcome.getId());
			fail();
		} catch (IllegalArgumentException e) {
			// expected
		}
		try {
			ourEncounterDao.read(new IdDt(outcome.getId().getIdPart()));
			fail();
		} catch (ResourceNotFoundException e) {
			// expected
		}

		// Now search by _id
		{
			SearchParameterMap paramMap = new SearchParameterMap();
			paramMap.add("_id", new StringParam(outcome.getId().getIdPart()));
			List<Patient> ret = toList(ourPatientDao.search(paramMap));
			assertEquals(1, ret.size());
			Patient p = ret.get(0);
			assertEquals("Tester", p.getNameFirstRep().getFamilyAsSingleString());
		}
		{
			SearchParameterMap paramMap = new SearchParameterMap();
			paramMap.add("_id", new StringParam(outcome.getId().getIdPart()));
			paramMap.add(Patient.SP_NAME, new StringParam("tester"));
			List<Patient> ret = toList(ourPatientDao.search(paramMap));
			assertEquals(1, ret.size());
			Patient p = ret.get(0);
			assertEquals("Tester", p.getNameFirstRep().getFamilyAsSingleString());
		}
		{
			SearchParameterMap paramMap = new SearchParameterMap();
			paramMap.add(Patient.SP_NAME, new StringParam("tester"));
			paramMap.add("_id", new StringParam(outcome.getId().getIdPart()));
			List<Patient> ret = toList(ourPatientDao.search(paramMap));
			assertEquals(1, ret.size());
			Patient p = ret.get(0);
			assertEquals("Tester", p.getNameFirstRep().getFamilyAsSingleString());
		}
		{
			SearchParameterMap paramMap = new SearchParameterMap();
			paramMap.add(Patient.SP_NAME, new StringParam("tester"));
			paramMap.add("_id", new StringParam("000"));
			List<Patient> ret = toList(ourPatientDao.search(paramMap));
			assertEquals(0, ret.size());
		}
	}

	@Test
	public void testOrganizationName() {

		//@formatter:off
		String inputStr = "{\"resourceType\":\"Organization\",\n" + 
				"                \"extension\":[\n" + 
				"                    {\n" + 
				"                        \"url\":\"http://fhir.connectinggta.ca/Profile/organization#providerIdPool\",\n" + 
				"                        \"valueUri\":\"urn:oid:2.16.840.1.113883.3.239.23.21.1\"\n" + 
				"                    }\n" + 
				"                ],\n" + 
				"                \"text\":{\n" + 
				"                    \"status\":\"empty\",\n" + 
				"                    \"div\":\"<div xmlns=\\\"http://www.w3.org/1999/xhtml\\\">No narrative template available for resource profile: http://fhir.connectinggta.ca/Profile/organization</div>\"\n" + 
				"                },\n" + 
				"                \"identifier\":[\n" + 
				"                    {\n" + 
				"                        \"use\":\"official\",\n" + 
				"                        \"label\":\"HSP 2.16.840.1.113883.3.239.23.21\",\n" + 
				"                        \"system\":\"urn:cgta:hsp_ids\",\n" + 
				"                        \"value\":\"urn:oid:2.16.840.1.113883.3.239.23.21\"\n" + 
				"                    }\n" + 
				"                ],\n" + 
				"                \"name\":\"Peterborough Regional Health Centre\"\n" + 
				"            }\n" + 
				"        }";
		//@formatter:on

		Set<Long> val = ourOrganizationDao.searchForIds("name", new StringParam("P"));
		int initial = val.size();

		Organization org = ourFhirCtx.newJsonParser().parseResource(Organization.class, inputStr);
		ourOrganizationDao.create(org);

		val = ourOrganizationDao.searchForIds("name", new StringParam("P"));
		assertEquals(initial + 1, val.size());

	}

	@Test
	public void testPersistResourceLink() {
		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("testPersistResourceLink01");
		IdDt patientId01 = ourPatientDao.create(patient).getId();

		Patient patient02 = new Patient();
		patient02.addIdentifier().setSystem("urn:system").setValue("testPersistResourceLink02");
		IdDt patientId02 = ourPatientDao.create(patient02).getId();

		Observation obs01 = new Observation();
		obs01.setApplies(new DateTimeDt(new Date()));
		obs01.setSubject(new ResourceReferenceDt(patientId01));
		IdDt obsId01 = ourObservationDao.create(obs01).getId();

		Observation obs02 = new Observation();
		obs02.setApplies(new DateTimeDt(new Date()));
		obs02.setSubject(new ResourceReferenceDt(patientId02));
		IdDt obsId02 = ourObservationDao.create(obs02).getId();

		// Create another type, that shouldn't be returned
		DiagnosticReport dr01 = new DiagnosticReport();
		dr01.setSubject(new ResourceReferenceDt(patientId01));
		IdDt drId01 = ourDiagnosticReportDao.create(dr01).getId();

		ourLog.info("P1[{}] P2[{}] O1[{}] O2[{}] D1[{}]", new Object[] { patientId01, patientId02, obsId01, obsId02, drId01 });

		List<Observation> result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam(patientId01.getIdPart())));
		assertEquals(1, result.size());
		assertEquals(obsId01.getIdPart(), result.get(0).getId().getIdPart());

		result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam(patientId02.getIdPart())));
		assertEquals(1, result.size());
		assertEquals(obsId02.getIdPart(), result.get(0).getId().getIdPart());

		result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam("999999999999")));
		assertEquals(0, result.size());

	}

	@Test
	public void testPersistContactPoint() {
		List<IResource> found = toList(ourPatientDao.search(Patient.SP_TELECOM, new TokenParam(null, "555-123-4567")));
		int initialSize2000 = found.size();

		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("testPersistContactPoint");
		patient.addTelecom().setValue("555-123-4567");
		ourPatientDao.create(patient);

		found = toList(ourPatientDao.search(Patient.SP_TELECOM, new TokenParam(null, "555-123-4567")));
		assertEquals(1 + initialSize2000, found.size());

	}

	@Test
	public void testPersistSearchParamDate() {
		List<Patient> found = toList(ourPatientDao.search(Patient.SP_BIRTHDATE, new DateParam(QuantityCompararatorEnum.GREATERTHAN, "2000-01-01")));
		int initialSize2000 = found.size();

		found = toList(ourPatientDao.search(Patient.SP_BIRTHDATE, new DateParam(QuantityCompararatorEnum.GREATERTHAN, "2002-01-01")));
		int initialSize2002 = found.size();

		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("001");
		patient.setBirthDate(new DateDt("2001-01-01"));

		ourPatientDao.create(patient);

		found = toList(ourPatientDao.search(Patient.SP_BIRTHDATE, new DateParam(QuantityCompararatorEnum.GREATERTHAN, "2000-01-01")));
		assertEquals(1 + initialSize2000, found.size());

		found = toList(ourPatientDao.search(Patient.SP_BIRTHDATE, new DateParam(QuantityCompararatorEnum.GREATERTHAN, "2002-01-01")));
		assertEquals(initialSize2002, found.size());

		// If this throws an exception, that would be an acceptable outcome as well..
		found = toList(ourPatientDao.search(Patient.SP_BIRTHDATE + "AAAA", new DateParam(QuantityCompararatorEnum.GREATERTHAN, "2000-01-01")));
		assertEquals(0, found.size());

	}

	@Test
	public void testPersistSearchParamObservationString() {
		Observation obs = new Observation();
		obs.getCode().addCoding().setSystem("foo").setCode("testPersistSearchParamQuantity");
		obs.setValue(new StringDt("AAAABBBB"));

		ourObservationDao.create(obs);

		List<Observation> found = toList(ourObservationDao.search("value-string", new StringDt("AAAABBBB")));
		assertEquals(1, found.size());

		found = toList(ourObservationDao.search("value-string", new StringDt("AAAABBBBCCC")));
		assertEquals(0, found.size());

	}

	@Test
	public void testPersistSearchParamQuantity() {
		Observation obs = new Observation();
		obs.getCode().addCoding().setSystem("foo").setCode("testPersistSearchParamQuantity");
		obs.setValue(new QuantityDt(111));

		ourObservationDao.create(obs);

		List<Observation> found = toList(ourObservationDao.search("value-quantity", new QuantityDt(111)));
		assertEquals(1, found.size());

		found = toList(ourObservationDao.search("value-quantity", new QuantityDt(112)));
		assertEquals(1, found.size());

		found = toList(ourObservationDao.search("value-quantity", new QuantityDt(212)));
		assertEquals(0, found.size());

	}

	@Test
	public void testPersistSearchParams() {
		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("001testPersistSearchParams");
		patient.getGenderElement().setValueAsEnum(AdministrativeGenderEnum.MALE);
		patient.addName().addFamily("Tester").addGiven("JoetestPersistSearchParams");

		MethodOutcome outcome = ourPatientDao.create(patient);
		assertNotNull(outcome.getId());
		assertFalse(outcome.getId().isEmpty());

		long id = outcome.getId().getIdPartAsLong();

		IdentifierDt value = new IdentifierDt("urn:system", "001testPersistSearchParams");
		List<Patient> found = toList(ourPatientDao.search(Patient.SP_IDENTIFIER, value));
		assertEquals(1, found.size());
		assertEquals(id, found.get(0).getId().getIdPartAsLong().longValue());

		// found = ourPatientDao.search(Patient.SP_GENDER, new IdentifierDt(null, "M"));
		// assertEquals(1, found.size());
		// assertEquals(id, found.get(0).getId().asLong().longValue());
		//
		// found = ourPatientDao.search(Patient.SP_GENDER, new IdentifierDt(null, "F"));
		// assertEquals(0, found.size());

		SearchParameterMap map = new SearchParameterMap();
		map.add(Patient.SP_IDENTIFIER, new IdentifierDt("urn:system", "001testPersistSearchParams"));
		map.add(Patient.SP_GENDER, new IdentifierDt("urn:some:wrong:system", AdministrativeGenderEnum.MALE.getCode()));
		found = toList(ourPatientDao.search(map));
		assertEquals(0, found.size());

		// Now with no system on the gender (should match)
		map = new SearchParameterMap();
		map.add(Patient.SP_IDENTIFIER, new IdentifierDt("urn:system", "001testPersistSearchParams"));
		map.add(Patient.SP_GENDER, new IdentifierDt(null, AdministrativeGenderEnum.MALE.getCode()));
		found = toList(ourPatientDao.search(map));
		assertEquals(1, found.size());
		assertEquals(id, found.get(0).getId().getIdPartAsLong().longValue());

		// Now with the wrong gender
		map = new SearchParameterMap();
		map.add(Patient.SP_IDENTIFIER, new IdentifierDt("urn:system", "001testPersistSearchParams"));
		map.add(Patient.SP_GENDER, new IdentifierDt(AdministrativeGenderEnum.MALE.getSystem(), AdministrativeGenderEnum.FEMALE.getCode()));
		found = toList(ourPatientDao.search(map));
		assertEquals(0, found.size());

	}

	@Test
	public void testReadForcedIdVersionHistory() throws InterruptedException {
		Patient p1 = new Patient();
		p1.addIdentifier().setSystem("urn:system").setValue("testReadVorcedIdVersionHistory01");
		p1.setId("testReadVorcedIdVersionHistory");
		IdDt p1id = ourPatientDao.update(p1).getId();
		assertEquals("testReadVorcedIdVersionHistory", p1id.getIdPart());

		p1.addIdentifier().setSystem("urn:system").setValue("testReadVorcedIdVersionHistory02");
		p1.setId(p1id);
		IdDt p1idv2 = ourPatientDao.update(p1).getId();
		assertEquals("testReadVorcedIdVersionHistory", p1idv2.getIdPart());

		assertNotEquals(p1id.getValue(), p1idv2.getValue());

		Patient v1 = ourPatientDao.read(p1id);
		assertEquals(1, v1.getIdentifier().size());

		Patient v2 = ourPatientDao.read(p1idv2);
		assertEquals(2, v2.getIdentifier().size());

	}

	@Test
	public void testReverseIncludes() {
		String methodName = "testReverseIncludes";
		Organization org = new Organization();
		org.setName("X" + methodName + "X");
		IdDt orgId = ourOrganizationDao.create(org).getId();

		Patient pat = new Patient();
		pat.addName().addFamily("X" + methodName + "X");
		pat.getManagingOrganization().setReference(orgId.toUnqualifiedVersionless());
		ourPatientDao.create(pat);

		SearchParameterMap map = new SearchParameterMap();
		map.add(Organization.SP_NAME, new StringParam("X" + methodName + "X"));
		map.setRevIncludes(Collections.singleton(Patient.INCLUDE_ORGANIZATION));
		IBundleProvider resultsP = ourOrganizationDao.search(map);
		assertEquals(2, resultsP.size());
		List<IBaseResource> results = resultsP.getResources(0, resultsP.size());
		assertEquals(2, results.size());
		assertEquals(Organization.class, results.get(0).getClass());
		assertEquals(Patient.class, results.get(1).getClass());
	}

	@Test
	public void testResourceInstanceMetaOperation() {
		deleteEverything();

		String methodName = "testResourceInstanceMetaOperation";
		IdDt id1, id2;
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue(methodName);
			patient.addName().addFamily("Tester").addGiven("Joe");
			id1 = ourPatientDao.create(patient).getId();

			MetaDt metaAdd = new MetaDt();
			metaAdd.addTag().setSystem((String) null).setCode("Dog").setDisplay("Puppies");
			metaAdd.addSecurity().setSystem("seclabel:sys:1").setCode("seclabel:code:1").setDisplay("seclabel:dis:1");
			metaAdd.addProfile("http://profile/1");
			ourPatientDao.metaAddOperation(id1, metaAdd);
		}
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue(methodName);
			patient.addName().addFamily("Tester").addGiven("Joe");
			TagList tagList = new TagList();
			tagList.addTag("http://foo", "Cat", "Kittens");
			ResourceMetadataKeyEnum.TAG_LIST.put(patient, tagList);

			List<BaseCodingDt> securityLabels = new ArrayList<BaseCodingDt>();
			securityLabels.add(new CodingDt().setSystem("seclabel:sys:2").setCode("seclabel:code:2").setDisplay("seclabel:dis:2"));
			ResourceMetadataKeyEnum.SECURITY_LABELS.put(patient, securityLabels);

			ArrayList<IdDt> profiles = new ArrayList<IdDt>();
			profiles.add(new IdDt("http://profile/2"));
			ResourceMetadataKeyEnum.PROFILES.put(patient, profiles);

			id2 = ourPatientDao.create(patient).getId();
		}
		{
			Device device = new Device();
			device.addIdentifier().setSystem("urn:system").setValue(methodName);
			TagList tagList = new TagList();
			tagList.addTag("http://foo", "Foo", "Bars");
			ResourceMetadataKeyEnum.TAG_LIST.put(device, tagList);

			List<BaseCodingDt> securityLabels = new ArrayList<BaseCodingDt>();
			securityLabels.add(new CodingDt().setSystem("seclabel:sys:3").setCode("seclabel:code:3").setDisplay("seclabel:dis:3"));
			ResourceMetadataKeyEnum.SECURITY_LABELS.put(device, securityLabels);

			ArrayList<IdDt> profiles = new ArrayList<IdDt>();
			profiles.add(new IdDt("http://profile/3"));
			ResourceMetadataKeyEnum.PROFILES.put(device, profiles);

			ourDeviceDao.create(device);
		}

		MetaDt meta;

		meta = ourPatientDao.metaGetOperation();
		List<CodingDt> published = meta.getTag();
		assertEquals(2, published.size());
		assertEquals(null, published.get(0).getSystem());
		assertEquals("Dog", published.get(0).getCode());
		assertEquals("Puppies", published.get(0).getDisplay());
		assertEquals("http://foo", published.get(1).getSystem());
		assertEquals("Cat", published.get(1).getCode());
		assertEquals("Kittens", published.get(1).getDisplay());
		List<CodingDt> secLabels = meta.getSecurity();
		assertEquals(2, secLabels.size());
		assertEquals("seclabel:sys:1", secLabels.get(0).getSystemElement().getValue());
		assertEquals("seclabel:code:1", secLabels.get(0).getCodeElement().getValue());
		assertEquals("seclabel:dis:1", secLabels.get(0).getDisplayElement().getValue());
		assertEquals("seclabel:sys:2", secLabels.get(1).getSystemElement().getValue());
		assertEquals("seclabel:code:2", secLabels.get(1).getCodeElement().getValue());
		assertEquals("seclabel:dis:2", secLabels.get(1).getDisplayElement().getValue());
		List<UriDt> profiles = meta.getProfile();
		assertEquals(2, profiles.size());
		assertEquals("http://profile/1", profiles.get(0).getValue());
		assertEquals("http://profile/2", profiles.get(1).getValue());

		meta = ourPatientDao.metaGetOperation(id2);
		published = meta.getTag();
		assertEquals(1, published.size());
		assertEquals("http://foo", published.get(0).getSystem());
		assertEquals("Cat", published.get(0).getCode());
		assertEquals("Kittens", published.get(0).getDisplay());
		secLabels = meta.getSecurity();
		assertEquals(1, secLabels.size());
		assertEquals("seclabel:sys:2", secLabels.get(0).getSystemElement().getValue());
		assertEquals("seclabel:code:2", secLabels.get(0).getCodeElement().getValue());
		assertEquals("seclabel:dis:2", secLabels.get(0).getDisplayElement().getValue());
		profiles = meta.getProfile();
		assertEquals(1, profiles.size());
		assertEquals("http://profile/2", profiles.get(0).getValue());

		{
			MetaDt metaDel = new MetaDt();
			metaDel.addTag().setSystem((String) null).setCode("Dog");
			metaDel.addSecurity().setSystem("seclabel:sys:1").setCode("seclabel:code:1");
			metaDel.addProfile("http://profile/1");
			ourPatientDao.metaDeleteOperation(id1, metaDel);
		}

		meta = ourPatientDao.metaGetOperation();
		published = meta.getTag();
		assertEquals(1, published.size());
		assertEquals("http://foo", published.get(0).getSystem());
		assertEquals("Cat", published.get(0).getCode());
		assertEquals("Kittens", published.get(0).getDisplay());
		secLabels = meta.getSecurity();
		assertEquals(1, secLabels.size());
		assertEquals("seclabel:sys:2", secLabels.get(0).getSystemElement().getValue());
		assertEquals("seclabel:code:2", secLabels.get(0).getCodeElement().getValue());
		assertEquals("seclabel:dis:2", secLabels.get(0).getDisplayElement().getValue());
		profiles = meta.getProfile();
		assertEquals(1, profiles.size());
		assertEquals("http://profile/2", profiles.get(0).getValue());

	}

	@Test
	public void testResourceMetaOperation() {
		deleteEverything();

		String methodName = "testResourceMetaOperation";
		IdDt id1, id2;
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue(methodName);
			patient.addName().addFamily("Tester").addGiven("Joe");
			TagList tagList = new TagList();
			tagList.addTag(null, "Dog", "Puppies");
			ResourceMetadataKeyEnum.TAG_LIST.put(patient, tagList);

			List<BaseCodingDt> securityLabels = new ArrayList<BaseCodingDt>();
			securityLabels.add(new CodingDt().setSystem("seclabel:sys:1").setCode("seclabel:code:1").setDisplay("seclabel:dis:1"));
			ResourceMetadataKeyEnum.SECURITY_LABELS.put(patient, securityLabels);

			ArrayList<IdDt> profiles = new ArrayList<IdDt>();
			profiles.add(new IdDt("http://profile/1"));
			ResourceMetadataKeyEnum.PROFILES.put(patient, profiles);

			id1 = ourPatientDao.create(patient).getId();
		}
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue(methodName);
			patient.addName().addFamily("Tester").addGiven("Joe");
			TagList tagList = new TagList();
			tagList.addTag("http://foo", "Cat", "Kittens");
			ResourceMetadataKeyEnum.TAG_LIST.put(patient, tagList);

			List<BaseCodingDt> securityLabels = new ArrayList<BaseCodingDt>();
			securityLabels.add(new CodingDt().setSystem("seclabel:sys:2").setCode("seclabel:code:2").setDisplay("seclabel:dis:2"));
			ResourceMetadataKeyEnum.SECURITY_LABELS.put(patient, securityLabels);

			ArrayList<IdDt> profiles = new ArrayList<IdDt>();
			profiles.add(new IdDt("http://profile/2"));
			ResourceMetadataKeyEnum.PROFILES.put(patient, profiles);

			id2 = ourPatientDao.create(patient).getId();
		}
		{
			Device device = new Device();
			device.addIdentifier().setSystem("urn:system").setValue(methodName);
			TagList tagList = new TagList();
			tagList.addTag("http://foo", "Foo", "Bars");
			ResourceMetadataKeyEnum.TAG_LIST.put(device, tagList);

			List<BaseCodingDt> securityLabels = new ArrayList<BaseCodingDt>();
			securityLabels.add(new CodingDt().setSystem("seclabel:sys:3").setCode("seclabel:code:3").setDisplay("seclabel:dis:3"));
			ResourceMetadataKeyEnum.SECURITY_LABELS.put(device, securityLabels);

			ArrayList<IdDt> profiles = new ArrayList<IdDt>();
			profiles.add(new IdDt("http://profile/3"));
			ResourceMetadataKeyEnum.PROFILES.put(device, profiles);

			ourDeviceDao.create(device);
		}

		MetaDt meta;

		meta = ourPatientDao.metaGetOperation();
		List<CodingDt> published = meta.getTag();
		assertEquals(2, published.size());
		assertEquals(null, published.get(0).getSystem());
		assertEquals("Dog", published.get(0).getCode());
		assertEquals("Puppies", published.get(0).getDisplay());
		assertEquals("http://foo", published.get(1).getSystem());
		assertEquals("Cat", published.get(1).getCode());
		assertEquals("Kittens", published.get(1).getDisplay());
		List<CodingDt> secLabels = meta.getSecurity();
		assertEquals(2, secLabels.size());
		assertEquals("seclabel:sys:1", secLabels.get(0).getSystemElement().getValue());
		assertEquals("seclabel:code:1", secLabels.get(0).getCodeElement().getValue());
		assertEquals("seclabel:dis:1", secLabels.get(0).getDisplayElement().getValue());
		assertEquals("seclabel:sys:2", secLabels.get(1).getSystemElement().getValue());
		assertEquals("seclabel:code:2", secLabels.get(1).getCodeElement().getValue());
		assertEquals("seclabel:dis:2", secLabels.get(1).getDisplayElement().getValue());
		List<UriDt> profiles = meta.getProfile();
		assertEquals(2, profiles.size());
		assertEquals("http://profile/1", profiles.get(0).getValue());
		assertEquals("http://profile/2", profiles.get(1).getValue());

		meta = ourPatientDao.metaGetOperation(id2);
		published = meta.getTag();
		assertEquals(1, published.size());
		assertEquals("http://foo", published.get(0).getSystem());
		assertEquals("Cat", published.get(0).getCode());
		assertEquals("Kittens", published.get(0).getDisplay());
		secLabels = meta.getSecurity();
		assertEquals(1, secLabels.size());
		assertEquals("seclabel:sys:2", secLabels.get(0).getSystemElement().getValue());
		assertEquals("seclabel:code:2", secLabels.get(0).getCodeElement().getValue());
		assertEquals("seclabel:dis:2", secLabels.get(0).getDisplayElement().getValue());
		profiles = meta.getProfile();
		assertEquals(1, profiles.size());
		assertEquals("http://profile/2", profiles.get(0).getValue());

		ourPatientDao.removeTag(id1, TagTypeEnum.TAG, null, "Dog");
		ourPatientDao.removeTag(id1, TagTypeEnum.SECURITY_LABEL, "seclabel:sys:1", "seclabel:code:1");
		ourPatientDao.removeTag(id1, TagTypeEnum.PROFILE, BaseFhirDao.NS_JPA_PROFILE, "http://profile/1");

		meta = ourPatientDao.metaGetOperation();
		published = meta.getTag();
		assertEquals(1, published.size());
		assertEquals("http://foo", published.get(0).getSystem());
		assertEquals("Cat", published.get(0).getCode());
		assertEquals("Kittens", published.get(0).getDisplay());
		secLabels = meta.getSecurity();
		assertEquals(1, secLabels.size());
		assertEquals("seclabel:sys:2", secLabels.get(0).getSystemElement().getValue());
		assertEquals("seclabel:code:2", secLabels.get(0).getCodeElement().getValue());
		assertEquals("seclabel:dis:2", secLabels.get(0).getDisplayElement().getValue());
		profiles = meta.getProfile();
		assertEquals(1, profiles.size());
		assertEquals("http://profile/2", profiles.get(0).getValue());

	}

	@Test
	public void testSearchAll() {
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("001");
			patient.addName().addFamily("Tester").addGiven("Joe");
			ourPatientDao.create(patient);
		}
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("002");
			patient.addName().addFamily("Tester").addGiven("John");
			ourPatientDao.create(patient);
		}

		Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
		List<Patient> patients = toList(ourPatientDao.search(params));
		assertTrue(patients.size() >= 2);
	}

	@Test
	public void testSearchByIdParam() {
		IdDt id1;
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("001");
			id1 = ourPatientDao.create(patient).getId();
		}
		IdDt id2;
		{
			Organization patient = new Organization();
			patient.addIdentifier().setSystem("urn:system").setValue("001");
			id2 = ourOrganizationDao.create(patient).getId();
		}

		Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
		params.put("_id", new StringDt(id1.getIdPart()));
		assertEquals(1, toList(ourPatientDao.search(params)).size());

		params.put("_id", new StringDt("9999999999999999"));
		assertEquals(0, toList(ourPatientDao.search(params)).size());

		params.put("_id", new StringDt(id2.getIdPart()));
		assertEquals(0, toList(ourPatientDao.search(params)).size());

	}

	@Test
	public void testSearchCompositeParam() {
		Observation o1 = new Observation();
		o1.getCode().addCoding().setSystem("foo").setCode("testSearchCompositeParamN01");
		o1.setValue(new StringDt("testSearchCompositeParamS01"));
		IdDt id1 = ourObservationDao.create(o1).getId();

		Observation o2 = new Observation();
		o2.getCode().addCoding().setSystem("foo").setCode("testSearchCompositeParamN01");
		o2.setValue(new StringDt("testSearchCompositeParamS02"));
		IdDt id2 = ourObservationDao.create(o2).getId();

		{
			TokenParam v0 = new TokenParam("foo", "testSearchCompositeParamN01");
			StringParam v1 = new StringParam("testSearchCompositeParamS01");
			CompositeParam<TokenParam, StringParam> val = new CompositeParam<TokenParam, StringParam>(v0, v1);
			IBundleProvider result = ourObservationDao.search(Observation.SP_CODE_VALUE_STRING, val);
			assertEquals(1, result.size());
			assertEquals(id1.toUnqualifiedVersionless(), result.getResources(0, 1).get(0).getIdElement().toUnqualifiedVersionless());
		}
		{
			TokenParam v0 = new TokenParam("foo", "testSearchCompositeParamN01");
			StringParam v1 = new StringParam("testSearchCompositeParamS02");
			CompositeParam<TokenParam, StringParam> val = new CompositeParam<TokenParam, StringParam>(v0, v1);
			IBundleProvider result = ourObservationDao.search(Observation.SP_CODE_VALUE_STRING, val);
			assertEquals(1, result.size());
			assertEquals(id2.toUnqualifiedVersionless(), result.getResources(0, 1).get(0).getIdElement().toUnqualifiedVersionless());
		}
	}

	@Test
	public void testSearchCompositeParamDate() {
		Observation o1 = new Observation();
		o1.getCode().addCoding().setSystem("foo").setCode("testSearchCompositeParamDateN01");
		o1.setValue(new PeriodDt().setStart(new DateTimeDt("2001-01-01T11:11:11")));
		IdDt id1 = ourObservationDao.create(o1).getId().toUnqualifiedVersionless();

		Observation o2 = new Observation();
		o2.getCode().addCoding().setSystem("foo").setCode("testSearchCompositeParamDateN01");
		o2.setValue(new PeriodDt().setStart(new DateTimeDt("2001-01-01T12:12:12")));
		IdDt id2 = ourObservationDao.create(o2).getId().toUnqualifiedVersionless();

		{
			TokenParam v0 = new TokenParam("foo", "testSearchCompositeParamDateN01");
			DateParam v1 = new DateParam("2001-01-01T11:11:11");
			CompositeParam<TokenParam, DateParam> val = new CompositeParam<TokenParam, DateParam>(v0, v1);
			IBundleProvider result = ourObservationDao.search(Observation.SP_CODE_VALUE_DATE, val);
			assertEquals(1, result.size());
			assertEquals(id1.toUnqualifiedVersionless(), result.getResources(0, 1).get(0).getIdElement().toUnqualifiedVersionless());
		}
		{
			TokenParam v0 = new TokenParam("foo", "testSearchCompositeParamDateN01");
			// TODO: this should also work with ">2001-01-01T15:12:12" since the two times only have a lower bound
			DateParam v1 = new DateParam(">2001-01-01T10:12:12");
			CompositeParam<TokenParam, DateParam> val = new CompositeParam<TokenParam, DateParam>(v0, v1);
			IBundleProvider result = ourObservationDao.search(Observation.SP_CODE_VALUE_DATE, val);
			assertEquals(2, result.size());
			assertThat(toUnqualifiedVersionlessIds(result), containsInAnyOrder(id1, id2));
		}

	}

	@Test
	public void testSearchForUnknownAlphanumericId() {
		{
			SearchParameterMap map = new SearchParameterMap();
			map.add("_id", new StringParam("testSearchForUnknownAlphanumericId"));
			IBundleProvider retrieved = ourPatientDao.search(map);
			assertEquals(0, retrieved.size());
		}
	}

	@Test
	public void testSearchLanguageParam() {
		IdDt id1;
		{
			Patient patient = new Patient();
			patient.getLanguage().setValue("en_CA");
			patient.addIdentifier().setSystem("urn:system").setValue("001");
			patient.addName().addFamily("testSearchLanguageParam").addGiven("Joe");
			id1 = ourPatientDao.create(patient).getId();
		}
		IdDt id2;
		{
			Patient patient = new Patient();
			patient.getLanguage().setValue("en_US");
			patient.addIdentifier().setSystem("urn:system").setValue("002");
			patient.addName().addFamily("testSearchLanguageParam").addGiven("John");
			id2 = ourPatientDao.create(patient).getId();
		}
		{
			Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
			params.put(Patient.SP_RES_LANGUAGE, new StringParam("en_CA"));
			List<Patient> patients = toList(ourPatientDao.search(params));
			assertEquals(1, patients.size());
			assertEquals(id1.toUnqualifiedVersionless(), patients.get(0).getId().toUnqualifiedVersionless());
		}
		{
			Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
			params.put(Patient.SP_RES_LANGUAGE, new StringParam("en_US"));
			List<Patient> patients = toList(ourPatientDao.search(params));
			assertEquals(1, patients.size());
			assertEquals(id2.toUnqualifiedVersionless(), patients.get(0).getId().toUnqualifiedVersionless());
		}
		{
			Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
			params.put(Patient.SP_RES_LANGUAGE, new StringParam("en_GB"));
			List<Patient> patients = toList(ourPatientDao.search(params));
			assertEquals(0, patients.size());
		}

	}

	@Test
	public void testSearchNameParam() {
		IdDt id1;
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("001");
			patient.addName().addFamily("testSearchNameParam01Fam").addGiven("testSearchNameParam01Giv");
			ResourceMetadataKeyEnum.TITLE.put(patient, "P1TITLE");
			id1 = ourPatientDao.create(patient).getId();
		}
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("002");
			patient.addName().addFamily("testSearchNameParam02Fam").addGiven("testSearchNameParam02Giv");
			ourPatientDao.create(patient);
		}

		Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
		params.put(Patient.SP_FAMILY, new StringDt("testSearchNameParam01Fam"));
		List<Patient> patients = toList(ourPatientDao.search(params));
		assertEquals(1, patients.size());
		assertEquals(id1.getIdPart(), patients.get(0).getId().getIdPart());
		assertEquals("P1TITLE", ResourceMetadataKeyEnum.TITLE.get(patients.get(0)));

		// Given name shouldn't return for family param
		params = new HashMap<String, IQueryParameterType>();
		params.put(Patient.SP_FAMILY, new StringDt("testSearchNameParam01Giv"));
		patients = toList(ourPatientDao.search(params));
		assertEquals(0, patients.size());

		params = new HashMap<String, IQueryParameterType>();
		params.put(Patient.SP_NAME, new StringDt("testSearchNameParam01Fam"));
		patients = toList(ourPatientDao.search(params));
		assertEquals(1, patients.size());
		assertEquals(id1.getIdPart(), patients.get(0).getId().getIdPart());

		params = new HashMap<String, IQueryParameterType>();
		params.put(Patient.SP_NAME, new StringDt("testSearchNameParam01Giv"));
		patients = toList(ourPatientDao.search(params));
		assertEquals(1, patients.size());
		assertEquals(id1.getIdPart(), patients.get(0).getId().getIdPart());

		params = new HashMap<String, IQueryParameterType>();
		params.put(Patient.SP_FAMILY, new StringDt("testSearchNameParam01Foo"));
		patients = toList(ourPatientDao.search(params));
		assertEquals(0, patients.size());

	}

	@Test
	public void testSearchNumberParam() {
		Encounter e1 = new Encounter();
		e1.addIdentifier().setSystem("foo").setValue("testSearchNumberParam01");
		e1.getLength().setSystem(BaseFhirDao.UCUM_NS).setCode("min").setValue(4.0 * 24 * 60);
		IdDt id1 = ourEncounterDao.create(e1).getId();

		Encounter e2 = new Encounter();
		e2.addIdentifier().setSystem("foo").setValue("testSearchNumberParam02");
		e2.getLength().setSystem(BaseFhirDao.UCUM_NS).setCode("year").setValue(2.0);
		IdDt id2 = ourEncounterDao.create(e2).getId();
		{
			IBundleProvider found = ourEncounterDao.search(Encounter.SP_LENGTH, new NumberParam(">2"));
			assertEquals(2, found.size());
			assertThat(toUnqualifiedVersionlessIds(found), containsInAnyOrder(id1.toUnqualifiedVersionless(), id2.toUnqualifiedVersionless()));
		}
		{
			IBundleProvider found = ourEncounterDao.search(Encounter.SP_LENGTH, new NumberParam("<1"));
			assertEquals(0, found.size());
		}
		{
			IBundleProvider found = ourEncounterDao.search(Encounter.SP_LENGTH, new NumberParam("2"));
			assertEquals(1, found.size());
			assertThat(toUnqualifiedVersionlessIds(found), containsInAnyOrder(id1.toUnqualifiedVersionless()));
		}
	}

	@Test
	public void testSearchResourceLinkWithChain() {
		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("testSearchResourceLinkWithChainXX");
		patient.addIdentifier().setSystem("urn:system").setValue("testSearchResourceLinkWithChain01");
		IdDt patientId01 = ourPatientDao.create(patient).getId();

		Patient patient02 = new Patient();
		patient02.addIdentifier().setSystem("urn:system").setValue("testSearchResourceLinkWithChainXX");
		patient02.addIdentifier().setSystem("urn:system").setValue("testSearchResourceLinkWithChain02");
		IdDt patientId02 = ourPatientDao.create(patient02).getId();

		Observation obs01 = new Observation();
		obs01.setApplies(new DateTimeDt(new Date()));
		obs01.setSubject(new ResourceReferenceDt(patientId01));
		IdDt obsId01 = ourObservationDao.create(obs01).getId();

		Observation obs02 = new Observation();
		obs02.setApplies(new DateTimeDt(new Date()));
		obs02.setSubject(new ResourceReferenceDt(patientId02));
		IdDt obsId02 = ourObservationDao.create(obs02).getId();

		// Create another type, that shouldn't be returned
		DiagnosticReport dr01 = new DiagnosticReport();
		dr01.setSubject(new ResourceReferenceDt(patientId01));
		IdDt drId01 = ourDiagnosticReportDao.create(dr01).getId();

		ourLog.info("P1[{}] P2[{}] O1[{}] O2[{}] D1[{}]", new Object[] { patientId01, patientId02, obsId01, obsId02, drId01 });

		List<Observation> result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam(Patient.SP_IDENTIFIER, "urn:system|testSearchResourceLinkWithChain01")));
		assertEquals(1, result.size());
		assertEquals(obsId01.getIdPart(), result.get(0).getId().getIdPart());

		result = toList(ourObservationDao.search(Observation.SP_PATIENT, new ReferenceParam(patientId01.getIdPart())));
		assertEquals(1, result.size());

		result = toList(ourObservationDao.search(Observation.SP_PATIENT, new ReferenceParam(patientId01.getIdPart())));
		assertEquals(1, result.size());

		result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam(Patient.SP_IDENTIFIER, "999999999999")));
		assertEquals(0, result.size());

		result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam(Patient.SP_IDENTIFIER, "urn:system|testSearchResourceLinkWithChainXX")));
		assertEquals(2, result.size());

		result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam(Patient.SP_IDENTIFIER, "testSearchResourceLinkWithChainXX")));
		assertEquals(2, result.size());

		result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam(Patient.SP_IDENTIFIER, "|testSearchResourceLinkWithChainXX")));
		assertEquals(0, result.size());

	}

	@Test
	public void testSearchResourceLinkWithChainWithMultipleTypes() {
		Patient patient = new Patient();
		patient.addName().addFamily("testSearchResourceLinkWithChainWithMultipleTypes01");
		patient.addName().addFamily("testSearchResourceLinkWithChainWithMultipleTypesXX");
		IdDt patientId01 = ourPatientDao.create(patient).getId();

		Location loc01 = new Location();
		loc01.getNameElement().setValue("testSearchResourceLinkWithChainWithMultipleTypes01");
		IdDt locId01 = ourLocationDao.create(loc01).getId();

		Observation obs01 = new Observation();
		obs01.setApplies(new DateTimeDt(new Date()));
		obs01.setSubject(new ResourceReferenceDt(patientId01));
		IdDt obsId01 = ourObservationDao.create(obs01).getId();

		Observation obs02 = new Observation();
		obs02.setApplies(new DateTimeDt(new Date()));
		obs02.setSubject(new ResourceReferenceDt(locId01));
		IdDt obsId02 = ourObservationDao.create(obs02).getId();

		ourLog.info("P1[{}] L1[{}] Obs1[{}] Obs2[{}]", new Object[] { patientId01, locId01, obsId01, obsId02 });

		List<Observation> result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam(Patient.SP_NAME, "testSearchResourceLinkWithChainWithMultipleTypes01")));
		assertEquals(2, result.size());

		result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam(Patient.SP_NAME, "testSearchResourceLinkWithChainWithMultipleTypesXX")));
		assertEquals(1, result.size());

		result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam(Patient.SP_NAME, "testSearchResourceLinkWithChainWithMultipleTypesYY")));
		assertEquals(0, result.size());

		result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam("Patient", Patient.SP_NAME, "testSearchResourceLinkWithChainWithMultipleTypes01")));
		assertEquals(1, result.size());
		assertEquals(obsId01.getIdPart(), result.get(0).getId().getIdPart());

	}

	@Test
	public void testSearchResourceLinkWithTextLogicalId() {
		Patient patient = new Patient();
		patient.setId("testSearchResourceLinkWithTextLogicalId01");
		patient.addIdentifier().setSystem("urn:system").setValue("testSearchResourceLinkWithTextLogicalIdXX");
		patient.addIdentifier().setSystem("urn:system").setValue("testSearchResourceLinkWithTextLogicalId01");
		IdDt patientId01 = ourPatientDao.update(patient).getId();

		Patient patient02 = new Patient();
		patient02.setId("testSearchResourceLinkWithTextLogicalId02");
		patient02.addIdentifier().setSystem("urn:system").setValue("testSearchResourceLinkWithTextLogicalIdXX");
		patient02.addIdentifier().setSystem("urn:system").setValue("testSearchResourceLinkWithTextLogicalId02");
		IdDt patientId02 = ourPatientDao.update(patient02).getId();

		Observation obs01 = new Observation();
		obs01.setApplies(new DateTimeDt(new Date()));
		obs01.setSubject(new ResourceReferenceDt(patientId01));
		IdDt obsId01 = ourObservationDao.create(obs01).getId();

		Observation obs02 = new Observation();
		obs02.setApplies(new DateTimeDt(new Date()));
		obs02.setSubject(new ResourceReferenceDt(patientId02));
		IdDt obsId02 = ourObservationDao.create(obs02).getId();

		// Create another type, that shouldn't be returned
		DiagnosticReport dr01 = new DiagnosticReport();
		dr01.setSubject(new ResourceReferenceDt(patientId01));
		IdDt drId01 = ourDiagnosticReportDao.create(dr01).getId();

		ourLog.info("P1[{}] P2[{}] O1[{}] O2[{}] D1[{}]", new Object[] { patientId01, patientId02, obsId01, obsId02, drId01 });

		List<Observation> result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam("testSearchResourceLinkWithTextLogicalId01")));
		assertEquals(1, result.size());
		assertEquals(obsId01.getIdPart(), result.get(0).getId().getIdPart());

		try {
			ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam("testSearchResourceLinkWithTextLogicalId99"));
			fail();
		} catch (ResourceNotFoundException e) {
			// good
		}

		/*
		 * TODO: it's kind of weird that we throw a 404 for textual IDs that don't exist, but just return an empty list for numeric IDs that don't exist
		 */

		result = toList(ourObservationDao.search(Observation.SP_SUBJECT, new ReferenceParam("999999999999999")));
		assertEquals(0, result.size());

	}

	@Test
	public void testSearchStringParam() {
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("001");
			patient.addName().addFamily("Tester_testSearchStringParam").addGiven("Joe");
			ourPatientDao.create(patient);
		}
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("002");
			patient.addName().addFamily("Tester_testSearchStringParam").addGiven("John");
			ourPatientDao.create(patient);
		}

		Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
		params.put(Patient.SP_FAMILY, new StringDt("Tester_testSearchStringParam"));
		List<Patient> patients = toList(ourPatientDao.search(params));
		assertEquals(2, patients.size());

		params.put(Patient.SP_FAMILY, new StringDt("FOO_testSearchStringParam"));
		patients = toList(ourPatientDao.search(params));
		assertEquals(0, patients.size());

	}

	@Test
	public void testSearchWithMissingString() {
		IdDt orgId = ourOrganizationDao.create(new Organization()).getId();
		IdDt notMissing;
		IdDt missing;
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("001");
			missing = ourPatientDao.create(patient).getId().toUnqualifiedVersionless();
		}
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("002");
			patient.addName().addFamily("Tester_testSearchStringParam").addGiven("John");
			patient.setBirthDate(new DateDt("2011-01-01"));
			patient.getManagingOrganization().setReference(orgId);
			notMissing = ourPatientDao.create(patient).getId().toUnqualifiedVersionless();
		}
		// String Param
		{
			HashMap<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
			StringParam param = new StringParam();
			param.setMissing(false);
			params.put(Patient.SP_FAMILY, param);
			List<IdDt> patients = toUnqualifiedVersionlessIds(ourPatientDao.search(params));
			assertThat(patients, not(containsInRelativeOrder(missing)));
			assertThat(patients, containsInRelativeOrder(notMissing));
		}
		{
			Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
			StringParam param = new StringParam();
			param.setMissing(true);
			params.put(Patient.SP_FAMILY, param);
			List<IdDt> patients = toUnqualifiedVersionlessIds(ourPatientDao.search(params));
			assertThat(patients, containsInRelativeOrder(missing));
			assertThat(patients, not(containsInRelativeOrder(notMissing)));
		}
	}

	@Test
	public void testSearchWithMissingQuantity() {
		IdDt notMissing;
		IdDt missing;
		{
			Observation obs = new Observation();
			obs.addIdentifier().setSystem("urn:system").setValue("001");
			missing = ourObservationDao.create(obs).getId().toUnqualifiedVersionless();
		}
		{
			Observation obs = new Observation();
			obs.addIdentifier().setSystem("urn:system").setValue("002");
			obs.setValue(new QuantityDt(123));
			notMissing = ourObservationDao.create(obs).getId().toUnqualifiedVersionless();
		}
		// Quantity Param
		{
			HashMap<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
			QuantityParam param = new QuantityParam();
			param.setMissing(false);
			params.put(Observation.SP_VALUE_QUANTITY, param);
			List<IdDt> patients = toUnqualifiedVersionlessIds(ourObservationDao.search(params));
			assertThat(patients, not(containsInRelativeOrder(missing)));
			assertThat(patients, containsInRelativeOrder(notMissing));
		}
		{
			Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
			QuantityParam param = new QuantityParam();
			param.setMissing(true);
			params.put(Observation.SP_VALUE_QUANTITY, param);
			List<IdDt> patients = toUnqualifiedVersionlessIds(ourObservationDao.search(params));
			assertThat(patients, containsInRelativeOrder(missing));
			assertThat(patients, not(containsInRelativeOrder(notMissing)));
		}
	}

	@Test
	public void testSearchWithToken() {
		IdDt notMissing;
		IdDt missing;
		{
			Observation obs = new Observation();
			obs.addIdentifier().setSystem("urn:system").setValue("001");
			missing = ourObservationDao.create(obs).getId().toUnqualifiedVersionless();
		}
		{
			Observation obs = new Observation();
			obs.addIdentifier().setSystem("urn:system").setValue("002");
			obs.getCode().addCoding().setSystem("urn:system").setCode("002");
			notMissing = ourObservationDao.create(obs).getId().toUnqualifiedVersionless();
		}
		// Token Param
		{
			HashMap<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
			TokenParam param = new TokenParam();
			param.setMissing(false);
			params.put(Observation.SP_CODE, param);
			List<IdDt> patients = toUnqualifiedVersionlessIds(ourObservationDao.search(params));
			assertThat(patients, not(containsInRelativeOrder(missing)));
			assertThat(patients, containsInRelativeOrder(notMissing));
		}
		{
			Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
			TokenParam param = new TokenParam();
			param.setMissing(true);
			params.put(Observation.SP_CODE, param);
			List<IdDt> patients = toUnqualifiedVersionlessIds(ourObservationDao.search(params));
			assertThat(patients, containsInRelativeOrder(missing));
			assertThat(patients, not(containsInRelativeOrder(notMissing)));
		}
	}

	@Test
	public void testSearchWithMissingDate() {
		IdDt orgId = ourOrganizationDao.create(new Organization()).getId();
		IdDt notMissing;
		IdDt missing;
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("001");
			missing = ourPatientDao.create(patient).getId().toUnqualifiedVersionless();
		}
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("002");
			patient.addName().addFamily("Tester_testSearchStringParam").addGiven("John");
			patient.setBirthDate(new DateDt("2011-01-01"));
			patient.getManagingOrganization().setReference(orgId);
			notMissing = ourPatientDao.create(patient).getId().toUnqualifiedVersionless();
		}
		// Date Param
		{
			HashMap<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
			DateParam param = new DateParam();
			param.setMissing(false);
			params.put(Patient.SP_BIRTHDATE, param);
			List<IdDt> patients = toUnqualifiedVersionlessIds(ourPatientDao.search(params));
			assertThat(patients, not(containsInRelativeOrder(missing)));
			assertThat(patients, containsInRelativeOrder(notMissing));
		}
		{
			Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
			DateParam param = new DateParam();
			param.setMissing(true);
			params.put(Patient.SP_BIRTHDATE, param);
			List<IdDt> patients = toUnqualifiedVersionlessIds(ourPatientDao.search(params));
			assertThat(patients, containsInRelativeOrder(missing));
			assertThat(patients, not(containsInRelativeOrder(notMissing)));
		}
	}

	@Test
	public void testSearchWithMissingReference() {
		IdDt orgId = ourOrganizationDao.create(new Organization()).getId().toUnqualifiedVersionless();
		IdDt notMissing;
		IdDt missing;
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("001");
			missing = ourPatientDao.create(patient).getId().toUnqualifiedVersionless();
		}
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("002");
			patient.addName().addFamily("Tester_testSearchStringParam").addGiven("John");
			patient.setBirthDate(new DateDt("2011-01-01"));
			patient.getManagingOrganization().setReference(orgId);
			notMissing = ourPatientDao.create(patient).getId().toUnqualifiedVersionless();
		}
		// Reference Param
		{
			HashMap<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
			ReferenceParam param = new ReferenceParam();
			param.setMissing(false);
			params.put(Patient.SP_ORGANIZATION, param);
			List<IdDt> patients = toUnqualifiedVersionlessIds(ourPatientDao.search(params));
			assertThat(patients, not(containsInRelativeOrder(missing)));
			assertThat(patients, containsInRelativeOrder(notMissing));
		}
		{
			Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
			ReferenceParam param = new ReferenceParam();
			param.setMissing(true);
			params.put(Patient.SP_ORGANIZATION, param);
			List<IdDt> patients = toUnqualifiedVersionlessIds(ourPatientDao.search(params));
			assertThat(patients, containsInRelativeOrder(missing));
			assertThat(patients, not(containsInRelativeOrder(notMissing)));
			assertThat(patients, not(containsInRelativeOrder(orgId)));
		}
	}

	@Test
	public void testSearchStringParamWithNonNormalized() {
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("001");
			patient.addName().addGiven("testSearchStringParamWithNonNormalized_h\u00F6ra");
			ourPatientDao.create(patient);
		}
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("002");
			patient.addName().addGiven("testSearchStringParamWithNonNormalized_HORA");
			ourPatientDao.create(patient);
		}

		Map<String, IQueryParameterType> params = new HashMap<String, IQueryParameterType>();
		params.put(Patient.SP_GIVEN, new StringDt("testSearchStringParamWithNonNormalized_hora"));
		List<Patient> patients = toList(ourPatientDao.search(params));
		assertEquals(2, patients.size());

		StringParam parameter = new StringParam("testSearchStringParamWithNonNormalized_hora");
		parameter.setExact(true);
		params.put(Patient.SP_GIVEN, parameter);
		patients = toList(ourPatientDao.search(params));
		assertEquals(0, patients.size());

	}

	@Test
	public void testSearchTokenParam() {
		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("testSearchTokenParam001");
		patient.addName().addFamily("Tester").addGiven("testSearchTokenParam1");
		patient.addCommunication().getLanguage().setText("testSearchTokenParamComText").addCoding().setCode("testSearchTokenParamCode").setSystem("testSearchTokenParamSystem")
				.setDisplay("testSearchTokenParamDisplay");
		ourPatientDao.create(patient);

		patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("testSearchTokenParam002");
		patient.addName().addFamily("Tester").addGiven("testSearchTokenParam2");
		ourPatientDao.create(patient);

		{
			SearchParameterMap map = new SearchParameterMap();
			map.add(Patient.SP_IDENTIFIER, new IdentifierDt("urn:system", "testSearchTokenParam001"));
			IBundleProvider retrieved = ourPatientDao.search(map);
			assertEquals(1, retrieved.size());
		}
		{
			SearchParameterMap map = new SearchParameterMap();
			map.add(Patient.SP_IDENTIFIER, new IdentifierDt(null, "testSearchTokenParam001"));
			IBundleProvider retrieved = ourPatientDao.search(map);
			assertEquals(1, retrieved.size());
		}
		{
			SearchParameterMap map = new SearchParameterMap();
			map.add(Patient.SP_LANGUAGE, new IdentifierDt("testSearchTokenParamSystem", "testSearchTokenParamCode"));
			assertEquals(1, ourPatientDao.search(map).size());
		}
		{
			SearchParameterMap map = new SearchParameterMap();
			map.add(Patient.SP_LANGUAGE, new TokenParam(null, "testSearchTokenParamCode", true));
			assertEquals(0, ourPatientDao.search(map).size());
		}
		{
			SearchParameterMap map = new SearchParameterMap();
			map.add(Patient.SP_LANGUAGE, new TokenParam(null, "testSearchTokenParamComText", true));
			assertEquals(1, ourPatientDao.search(map).size());
		}
		{
			SearchParameterMap map = new SearchParameterMap();
			TokenOrListParam listParam = new TokenOrListParam();
			listParam.add(new IdentifierDt("urn:system", "testSearchTokenParam001"));
			listParam.add(new IdentifierDt("urn:system", "testSearchTokenParam002"));
			map.add(Patient.SP_IDENTIFIER, listParam);
			IBundleProvider retrieved = ourPatientDao.search(map);
			assertEquals(2, retrieved.size());
		}
		{
			SearchParameterMap map = new SearchParameterMap();
			TokenOrListParam listParam = new TokenOrListParam();
			listParam.add(new IdentifierDt(null, "testSearchTokenParam001"));
			listParam.add(new IdentifierDt("urn:system", "testSearchTokenParam002"));
			map.add(Patient.SP_IDENTIFIER, listParam);
			IBundleProvider retrieved = ourPatientDao.search(map);
			assertEquals(2, retrieved.size());
		}
	}

	@Test
	public void testSearchValueQuantity() {
		String methodName = "testSearchValueQuantity";

		QuantityParam param;
		Set<Long> found;
		param = new QuantityParam(QuantityCompararatorEnum.GREATERTHAN_OR_EQUALS, new BigDecimal("10"), null, null);
		found = ourObservationDao.searchForIds("value-quantity", param);
		int initialSize = found.size();

		Observation o = new Observation();
		o.getCode().addCoding().setSystem("urn:foo").setCode(methodName + "code");
		QuantityDt q = new QuantityDt().setSystem("urn:bar:" + methodName).setCode(methodName + "units").setValue(100);
		o.setValue(q);

		ourObservationDao.create(o);

		param = new QuantityParam(QuantityCompararatorEnum.GREATERTHAN_OR_EQUALS, new BigDecimal("10"), null, null);
		found = ourObservationDao.searchForIds("value-quantity", param);
		assertEquals(1 + initialSize, found.size());

		param = new QuantityParam(QuantityCompararatorEnum.GREATERTHAN_OR_EQUALS, new BigDecimal("10"), null, methodName + "units");
		found = ourObservationDao.searchForIds("value-quantity", param);
		assertEquals(1, found.size());

		param = new QuantityParam(QuantityCompararatorEnum.GREATERTHAN_OR_EQUALS, new BigDecimal("10"), "urn:bar:" + methodName, null);
		found = ourObservationDao.searchForIds("value-quantity", param);
		assertEquals(1, found.size());

		param = new QuantityParam(QuantityCompararatorEnum.GREATERTHAN_OR_EQUALS, new BigDecimal("10"), "urn:bar:" + methodName, methodName + "units");
		found = ourObservationDao.searchForIds("value-quantity", param);
		assertEquals(1, found.size());

	}

	@Test
	public void testSearchWithIncludes() {
		IdDt parentOrgId;
		{
			Organization org = new Organization();
			org.getNameElement().setValue("testSearchWithIncludes_O1Parent");
			parentOrgId = ourOrganizationDao.create(org).getId();
		}
		{
			Organization org = new Organization();
			org.getNameElement().setValue("testSearchWithIncludes_O1");
			org.setPartOf(new ResourceReferenceDt(parentOrgId));
			IdDt orgId = ourOrganizationDao.create(org).getId();

			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("001");
			patient.addName().addFamily("Tester_testSearchWithIncludes_P1").addGiven("Joe");
			patient.getManagingOrganization().setReference(orgId);
			ourPatientDao.create(patient);
		}
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("002");
			patient.addName().addFamily("Tester_testSearchWithIncludes_P2").addGiven("John");
			ourPatientDao.create(patient);
		}

		{
			// No includes
			SearchParameterMap params = new SearchParameterMap();
			params.add(Patient.SP_FAMILY, new StringDt("Tester_testSearchWithIncludes_P1"));
			List<IResource> patients = toList(ourPatientDao.search(params));
			assertEquals(1, patients.size());
		}
		{
			// Named include
			SearchParameterMap params = new SearchParameterMap();
			params.add(Patient.SP_FAMILY, new StringDt("Tester_testSearchWithIncludes_P1"));
			params.addInclude(Patient.INCLUDE_ORGANIZATION);
			IBundleProvider search = ourPatientDao.search(params);
			List<IResource> patients = toList(search);
			assertEquals(2, patients.size());
			assertEquals(Patient.class, patients.get(0).getClass());
			assertEquals(Organization.class, patients.get(1).getClass());
		}
		{
			// Named include with parent
			SearchParameterMap params = new SearchParameterMap();
			params.add(Patient.SP_FAMILY, new StringDt("Tester_testSearchWithIncludes_P1"));
			params.addInclude(Patient.INCLUDE_ORGANIZATION);
			params.addInclude(Organization.INCLUDE_PARTOF);
			IBundleProvider search = ourPatientDao.search(params);
			List<IResource> patients = toList(search);
			assertEquals(3, patients.size());
			assertEquals(Patient.class, patients.get(0).getClass());
			assertEquals(Organization.class, patients.get(1).getClass());
			assertEquals(Organization.class, patients.get(2).getClass());
		}
		{
			// * include
			SearchParameterMap params = new SearchParameterMap();
			params.add(Patient.SP_FAMILY, new StringDt("Tester_testSearchWithIncludes_P1"));
			params.addInclude(new Include("*"));
			IBundleProvider search = ourPatientDao.search(params);
			List<IResource> patients = toList(search);
			assertEquals(3, patients.size());
			assertEquals(Patient.class, patients.get(0).getClass());
			assertEquals(Organization.class, patients.get(1).getClass());
			assertEquals(Organization.class, patients.get(2).getClass());
		}
		{
			// Irrelevant include
			SearchParameterMap params = new SearchParameterMap();
			params.add(Patient.SP_FAMILY, new StringDt("Tester_testSearchWithIncludes_P1"));
			params.addInclude(Encounter.INCLUDE_INDICATION);
			IBundleProvider search = ourPatientDao.search(params);
			List<IResource> patients = toList(search);
			assertEquals(1, patients.size());
			assertEquals(Patient.class, patients.get(0).getClass());
		}
	}

	/**
	 * Test for #62
	 */
	@Test
	public void testSearchWithIncludesThatHaveTextId() {
		{
			Organization org = new Organization();
			org.setId("testSearchWithIncludesThatHaveTextId_id1");
			org.getNameElement().setValue("testSearchWithIncludesThatHaveTextId_O1");
			IdDt orgId = ourOrganizationDao.update(org).getId();
			assertThat(orgId.getValue(), endsWith("Organization/testSearchWithIncludesThatHaveTextId_id1/_history/1"));

			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("001");
			patient.addName().addFamily("Tester_testSearchWithIncludesThatHaveTextId_P1").addGiven("Joe");
			patient.getManagingOrganization().setReference(orgId);
			ourPatientDao.create(patient);
		}
		{
			Patient patient = new Patient();
			patient.addIdentifier().setSystem("urn:system").setValue("002");
			patient.addName().addFamily("Tester_testSearchWithIncludesThatHaveTextId_P2").addGiven("John");
			ourPatientDao.create(patient);
		}

		SearchParameterMap params = new SearchParameterMap();
		params.add(Patient.SP_FAMILY, new StringDt("Tester_testSearchWithIncludesThatHaveTextId_P1"));
		params.addInclude(Patient.INCLUDE_ORGANIZATION);
		IBundleProvider search = ourPatientDao.search(params);
		List<IResource> patients = toList(search);
		assertEquals(2, patients.size());
		assertEquals(Patient.class, patients.get(0).getClass());
		assertEquals(Organization.class, patients.get(1).getClass());

		params = new SearchParameterMap();
		params.add(Patient.SP_FAMILY, new StringDt("Tester_testSearchWithIncludesThatHaveTextId_P1"));
		patients = toList(ourPatientDao.search(params));
		assertEquals(1, patients.size());

	}

	@Test
	public void testSortByDate() {
		Patient p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue("testtestSortByDate");
		p.addName().addFamily("testSortF1").addGiven("testSortG1");
		p.setBirthDate(new DateDt("2001-01-01"));
		IdDt id1 = ourPatientDao.create(p).getId().toUnqualifiedVersionless();

		// Create out of order
		p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue("testtestSortByDate");
		p.addName().addFamily("testSortF2").addGiven("testSortG2");
		p.setBirthDate(new DateDt("2001-01-03"));
		IdDt id3 = ourPatientDao.create(p).getId().toUnqualifiedVersionless();

		p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue("testtestSortByDate");
		p.addName().addFamily("testSortF3").addGiven("testSortG3");
		p.setBirthDate(new DateDt("2001-01-02"));
		IdDt id2 = ourPatientDao.create(p).getId().toUnqualifiedVersionless();

		p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue("testtestSortByDate");
		IdDt id4 = ourPatientDao.create(p).getId().toUnqualifiedVersionless();

		List<IdDt> actual;
		SearchParameterMap pm;

		pm = new SearchParameterMap();
		pm.add(Patient.SP_IDENTIFIER, new TokenParam("urn:system", "testtestSortByDate"));
		pm.setSort(new SortSpec(Patient.SP_BIRTHDATE));
		actual = toUnqualifiedVersionlessIds(ourPatientDao.search(pm));
		assertEquals(4, actual.size());
		assertThat(actual, contains(id1, id2, id3, id4));

		pm = new SearchParameterMap();
		pm.add(Patient.SP_IDENTIFIER, new TokenParam("urn:system", "testtestSortByDate"));
		pm.setSort(new SortSpec(Patient.SP_BIRTHDATE).setOrder(SortOrderEnum.ASC));
		actual = toUnqualifiedVersionlessIds(ourPatientDao.search(pm));
		assertEquals(4, actual.size());
		assertThat(actual, contains(id1, id2, id3, id4));

		pm = new SearchParameterMap();
		pm.add(Patient.SP_IDENTIFIER, new TokenParam("urn:system", "testtestSortByDate"));
		pm.setSort(new SortSpec(Patient.SP_BIRTHDATE).setOrder(SortOrderEnum.DESC));
		actual = toUnqualifiedVersionlessIds(ourPatientDao.search(pm));
		assertEquals(4, actual.size());
		assertThat(actual, contains(id3, id2, id1, id4));

	}

	@Test
	public void testSortById() {
		String methodName = "testSortBTyId";

		Patient p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue(methodName);
		IdDt id1 = ourPatientDao.create(p).getId().toUnqualifiedVersionless();

		p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue(methodName);
		IdDt id2 = ourPatientDao.create(p).getId().toUnqualifiedVersionless();

		p = new Patient();
		p.setId(methodName);
		p.addIdentifier().setSystem("urn:system").setValue(methodName);
		IdDt idMethodName = ourPatientDao.update(p).getId().toUnqualifiedVersionless();
		assertEquals(methodName, idMethodName.getIdPart());

		p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue(methodName);
		IdDt id3 = ourPatientDao.create(p).getId().toUnqualifiedVersionless();

		p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue(methodName);
		IdDt id4 = ourPatientDao.create(p).getId().toUnqualifiedVersionless();

		SearchParameterMap pm;
		List<IdDt> actual;

		pm = new SearchParameterMap();
		pm.add(Patient.SP_IDENTIFIER, new TokenParam("urn:system", methodName));
		pm.setSort(new SortSpec(Patient.SP_RES_ID));
		actual = toUnqualifiedVersionlessIds(ourPatientDao.search(pm));
		assertEquals(5, actual.size());
		assertThat(actual, contains(idMethodName, id1, id2, id3, id4));

		pm = new SearchParameterMap();
		pm.add(Patient.SP_IDENTIFIER, new TokenParam("urn:system", methodName));
		pm.setSort(new SortSpec(Patient.SP_RES_ID).setOrder(SortOrderEnum.ASC));
		actual = toUnqualifiedVersionlessIds(ourPatientDao.search(pm));
		assertEquals(5, actual.size());
		assertThat(actual, contains(idMethodName, id1, id2, id3, id4));

		pm = new SearchParameterMap();
		pm.add(Patient.SP_IDENTIFIER, new TokenParam("urn:system", methodName));
		pm.setSort(new SortSpec(Patient.SP_RES_ID).setOrder(SortOrderEnum.DESC));
		actual = toUnqualifiedVersionlessIds(ourPatientDao.search(pm));
		assertEquals(5, actual.size());
		assertThat(actual, contains(id4, id3, id2, id1, idMethodName));
	}

	@Test
	public void testSortByString() {
		Patient p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue("testSortByString");
		p.addName().addFamily("testSortF1").addGiven("testSortG1");
		IdDt id1 = ourPatientDao.create(p).getId().toUnqualifiedVersionless();

		// Create out of order
		p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue("testSortByString");
		p.addName().addFamily("testSortF3").addGiven("testSortG3");
		IdDt id3 = ourPatientDao.create(p).getId().toUnqualifiedVersionless();

		p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue("testSortByString");
		p.addName().addFamily("testSortF2").addGiven("testSortG2");
		IdDt id2 = ourPatientDao.create(p).getId().toUnqualifiedVersionless();

		p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue("testSortByString");
		IdDt id4 = ourPatientDao.create(p).getId().toUnqualifiedVersionless();

		SearchParameterMap pm;
		List<IdDt> actual;

		pm = new SearchParameterMap();
		pm.add(Patient.SP_IDENTIFIER, new TokenParam("urn:system", "testSortByString"));
		pm.setSort(new SortSpec(Patient.SP_FAMILY));
		actual = toUnqualifiedVersionlessIds(ourPatientDao.search(pm));
		assertEquals(4, actual.size());
		assertThat(actual, contains(id1, id2, id3, id4));

		pm = new SearchParameterMap();
		pm.add(Patient.SP_IDENTIFIER, new TokenParam("urn:system", "testSortByString"));
		pm.setSort(new SortSpec(Patient.SP_FAMILY).setOrder(SortOrderEnum.ASC));
		actual = toUnqualifiedVersionlessIds(ourPatientDao.search(pm));
		assertEquals(4, actual.size());
		assertThat(actual, contains(id1, id2, id3, id4));

		pm = new SearchParameterMap();
		pm.add(Patient.SP_IDENTIFIER, new TokenParam("urn:system", "testSortByString"));
		pm.setSort(new SortSpec(Patient.SP_FAMILY).setOrder(SortOrderEnum.DESC));
		actual = toUnqualifiedVersionlessIds(ourPatientDao.search(pm));
		assertEquals(4, actual.size());
		assertThat(actual, contains(id3, id2, id1, id4));
	}

	@Test
	public void testStoreUnversionedResources() {
		Organization o1 = new Organization();
		o1.getNameElement().setValue("AAA");
		IdDt o1id = ourOrganizationDao.create(o1).getId();
		assertTrue(o1id.hasVersionIdPart());

		Patient p1 = new Patient();
		p1.addName().addFamily("AAAA");
		p1.getManagingOrganization().setReference(o1id);
		IdDt p1id = ourPatientDao.create(p1).getId();

		p1 = ourPatientDao.read(p1id);

		assertFalse(p1.getManagingOrganization().getReference().hasVersionIdPart());
		assertEquals(o1id.toUnqualifiedVersionless(), p1.getManagingOrganization().getReference().toUnqualifiedVersionless());
	}

	/**
	 * Test for issue #60
	 */
	@Test
	public void testStoreUtf8Characters() throws Exception {
		Organization org = new Organization();
		org.setName("測試醫院");
		org.addIdentifier().setSystem("urn:system").setValue("testStoreUtf8Characters_01");
		IdDt orgId = ourOrganizationDao.create(org).getId();

		Organization returned = ourOrganizationDao.read(orgId);
		String val = ourFhirCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(returned);

		ourLog.info(val);
		assertThat(val, containsString("<name value=\"測試醫院\"/>"));
	}

	@Test
	public void testStringParamWhichIsTooLong() {

		Organization org = new Organization();
		String str = "testStringParamLong__lvdaoy843s89tll8gvs89l4s3gelrukveilufyebrew8r87bv4b77feli7fsl4lv3vb7rexloxe7olb48vov4o78ls7bvo7vb48o48l4bb7vbvx";
		str = str + str;
		org.getNameElement().setValue(str);

		assertThat(str.length(), greaterThan(ResourceIndexedSearchParamString.MAX_LENGTH));

		Set<Long> val = ourOrganizationDao.searchForIds("name", new StringParam("P"));
		int initial = val.size();

		ourOrganizationDao.create(org);

		val = ourOrganizationDao.searchForIds("name", new StringParam("P"));
		assertEquals(initial + 0, val.size());

		val = ourOrganizationDao.searchForIds("name", new StringParam(str.substring(0, ResourceIndexedSearchParamString.MAX_LENGTH)));
		assertEquals(initial + 1, val.size());

		try {
			ourOrganizationDao.searchForIds("name", new StringParam(str.substring(0, ResourceIndexedSearchParamString.MAX_LENGTH + 1)));
			fail();
		} catch (InvalidRequestException e) {
			// ok
		}
	}

	@Test
	public void testTagsAndProfilesAndSecurityLabelsWithCreateAndReadAndSearch() {
		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("testTagsWithCreateAndReadAndSearch");
		patient.addName().addFamily("Tester").addGiven("Joe");
		TagList tagList = new TagList();
		tagList.addTag(null, "Dog", "Puppies");
		// Add this twice
		tagList.addTag("http://foo", "Cat", "Kittens");
		tagList.addTag("http://foo", "Cat", "Kittens");
		ResourceMetadataKeyEnum.TAG_LIST.put(patient, tagList);

		List<BaseCodingDt> securityLabels = new ArrayList<BaseCodingDt>();
		securityLabels.add(new CodingDt().setSystem("seclabel:sys:1").setCode("seclabel:code:1").setDisplay("seclabel:dis:1"));
		securityLabels.add(new CodingDt().setSystem("seclabel:sys:2").setCode("seclabel:code:2").setDisplay("seclabel:dis:2"));
		ResourceMetadataKeyEnum.SECURITY_LABELS.put(patient, securityLabels);

		ArrayList<IdDt> profiles = new ArrayList<IdDt>();
		profiles.add(new IdDt("http://profile/1"));
		profiles.add(new IdDt("http://profile/2"));
		ResourceMetadataKeyEnum.PROFILES.put(patient, profiles);

		MethodOutcome outcome = ourPatientDao.create(patient);
		IdDt patientId = outcome.getId();
		assertNotNull(patientId);
		assertFalse(patientId.isEmpty());

		Patient retrieved = ourPatientDao.read(patientId);
		TagList published = (TagList) retrieved.getResourceMetadata().get(ResourceMetadataKeyEnum.TAG_LIST);
		assertEquals(2, published.size());
		assertEquals("Dog", published.get(0).getTerm());
		assertEquals("Puppies", published.get(0).getLabel());
		assertEquals(null, published.get(0).getScheme());
		assertEquals("Cat", published.get(1).getTerm());
		assertEquals("Kittens", published.get(1).getLabel());
		assertEquals("http://foo", published.get(1).getScheme());
		assertEquals(2, ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).size());
		assertEquals("seclabel:sys:1", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(0).getSystemElement().getValue());
		assertEquals("seclabel:code:1", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(0).getCodeElement().getValue());
		assertEquals("seclabel:dis:1", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(0).getDisplayElement().getValue());
		assertEquals("seclabel:sys:2", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(1).getSystemElement().getValue());
		assertEquals("seclabel:code:2", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(1).getCodeElement().getValue());
		assertEquals("seclabel:dis:2", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(1).getDisplayElement().getValue());
		assertEquals(2, ResourceMetadataKeyEnum.PROFILES.get(retrieved).size());
		assertEquals("http://profile/1", ResourceMetadataKeyEnum.PROFILES.get(retrieved).get(0).getValue());
		assertEquals("http://profile/2", ResourceMetadataKeyEnum.PROFILES.get(retrieved).get(1).getValue());

		List<Patient> search = toList(ourPatientDao.search(Patient.SP_IDENTIFIER, patient.getIdentifierFirstRep()));
		assertEquals(1, search.size());
		retrieved = search.get(0);
		published = (TagList) retrieved.getResourceMetadata().get(ResourceMetadataKeyEnum.TAG_LIST);
		assertEquals("Dog", published.get(0).getTerm());
		assertEquals("Puppies", published.get(0).getLabel());
		assertEquals(null, published.get(0).getScheme());
		assertEquals("Cat", published.get(1).getTerm());
		assertEquals("Kittens", published.get(1).getLabel());
		assertEquals("http://foo", published.get(1).getScheme());
		assertEquals(2, ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).size());
		assertEquals("seclabel:sys:1", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(0).getSystemElement().getValue());
		assertEquals("seclabel:code:1", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(0).getCodeElement().getValue());
		assertEquals("seclabel:dis:1", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(0).getDisplayElement().getValue());
		assertEquals("seclabel:sys:2", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(1).getSystemElement().getValue());
		assertEquals("seclabel:code:2", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(1).getCodeElement().getValue());
		assertEquals("seclabel:dis:2", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(1).getDisplayElement().getValue());
		assertEquals(2, ResourceMetadataKeyEnum.PROFILES.get(retrieved).size());
		assertEquals("http://profile/1", ResourceMetadataKeyEnum.PROFILES.get(retrieved).get(0).getValue());
		assertEquals("http://profile/2", ResourceMetadataKeyEnum.PROFILES.get(retrieved).get(1).getValue());

		ourPatientDao.addTag(patientId, TagTypeEnum.TAG, "http://foo", "Cat", "Kittens");
		ourPatientDao.addTag(patientId, TagTypeEnum.TAG, "http://foo", "Cow", "Calves");

		retrieved = ourPatientDao.read(patientId);
		published = (TagList) retrieved.getResourceMetadata().get(ResourceMetadataKeyEnum.TAG_LIST);
		assertEquals(3, published.size());
		assertEquals("Dog", published.get(0).getTerm());
		assertEquals("Puppies", published.get(0).getLabel());
		assertEquals(null, published.get(0).getScheme());
		assertEquals("Cat", published.get(1).getTerm());
		assertEquals("Kittens", published.get(1).getLabel());
		assertEquals("http://foo", published.get(1).getScheme());
		assertEquals("Cow", published.get(2).getTerm());
		assertEquals("Calves", published.get(2).getLabel());
		assertEquals("http://foo", published.get(2).getScheme());
		assertEquals(2, ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).size());
		assertEquals("seclabel:sys:1", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(0).getSystemElement().getValue());
		assertEquals("seclabel:code:1", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(0).getCodeElement().getValue());
		assertEquals("seclabel:dis:1", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(0).getDisplayElement().getValue());
		assertEquals("seclabel:sys:2", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(1).getSystemElement().getValue());
		assertEquals("seclabel:code:2", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(1).getCodeElement().getValue());
		assertEquals("seclabel:dis:2", ResourceMetadataKeyEnum.SECURITY_LABELS.get(retrieved).get(1).getDisplayElement().getValue());
		assertEquals(2, ResourceMetadataKeyEnum.PROFILES.get(retrieved).size());
		assertEquals("http://profile/1", ResourceMetadataKeyEnum.PROFILES.get(retrieved).get(0).getValue());
		assertEquals("http://profile/2", ResourceMetadataKeyEnum.PROFILES.get(retrieved).get(1).getValue());

	}

	@Test
	public void testTokenParamWhichIsTooLong() {

		String longStr1 = RandomStringUtils.randomAlphanumeric(ResourceIndexedSearchParamString.MAX_LENGTH + 100);
		String longStr2 = RandomStringUtils.randomAlphanumeric(ResourceIndexedSearchParamString.MAX_LENGTH + 100);

		Organization org = new Organization();
		org.getNameElement().setValue("testTokenParamWhichIsTooLong");
		org.getType().addCoding().setSystem(longStr1).setCode(longStr2);

		String subStr1 = longStr1.substring(0, ResourceIndexedSearchParamString.MAX_LENGTH);
		String subStr2 = longStr2.substring(0, ResourceIndexedSearchParamString.MAX_LENGTH);
		Set<Long> val = ourOrganizationDao.searchForIds("type", new IdentifierDt(subStr1, subStr2));
		int initial = val.size();

		ourOrganizationDao.create(org);

		val = ourOrganizationDao.searchForIds("type", new IdentifierDt(subStr1, subStr2));
		assertEquals(initial + 1, val.size());

		try {
			ourOrganizationDao.searchForIds("type", new IdentifierDt(longStr1, subStr2));
			fail();
		} catch (InvalidRequestException e) {
			// ok
		}

		try {
			ourOrganizationDao.searchForIds("type", new IdentifierDt(subStr1, longStr2));
			fail();
		} catch (InvalidRequestException e) {
			// ok
		}
	}

	@Test
	public void testUpdateAndGetHistoryResource() throws InterruptedException {
		Patient patient = new Patient();
		patient.addIdentifier().setSystem("urn:system").setValue("001");
		patient.addName().addFamily("Tester").addGiven("Joe");

		MethodOutcome outcome = ourPatientDao.create(patient);
		assertNotNull(outcome.getId());
		assertFalse(outcome.getId().isEmpty());

		assertEquals("1", outcome.getId().getVersionIdPart());

		Date now = new Date();
		Patient retrieved = ourPatientDao.read(outcome.getId());
		InstantDt published = (InstantDt) retrieved.getResourceMetadata().get(ResourceMetadataKeyEnum.PUBLISHED);
		InstantDt updated = (InstantDt) retrieved.getResourceMetadata().get(ResourceMetadataKeyEnum.UPDATED);
		assertTrue(published.before(now));
		assertTrue(updated.before(now));

		Thread.sleep(1000);

		retrieved.getIdentifierFirstRep().setValue("002");
		MethodOutcome outcome2 = ourPatientDao.update(retrieved);
		assertEquals(outcome.getId().getIdPart(), outcome2.getId().getIdPart());
		assertNotEquals(outcome.getId().getVersionIdPart(), outcome2.getId().getVersionIdPart());

		assertEquals("2", outcome2.getId().getVersionIdPart());

		Date now2 = new Date();

		Patient retrieved2 = ourPatientDao.read(outcome.getId().toVersionless());

		assertEquals("2", retrieved2.getId().getVersionIdPart());
		assertEquals("002", retrieved2.getIdentifierFirstRep().getValue());
		InstantDt published2 = (InstantDt) retrieved2.getResourceMetadata().get(ResourceMetadataKeyEnum.PUBLISHED);
		InstantDt updated2 = (InstantDt) retrieved2.getResourceMetadata().get(ResourceMetadataKeyEnum.UPDATED);
		assertTrue(published2.before(now));
		assertTrue(updated2.after(now));
		assertTrue(updated2.before(now2));

		Thread.sleep(2000);

		/*
		 * Get history
		 */

		IBundleProvider historyBundle = ourPatientDao.history(outcome.getId(), null);

		assertEquals(2, historyBundle.size());

		List<IBaseResource> history = historyBundle.getResources(0, 2);
		assertEquals("1", history.get(1).getIdElement().getVersionIdPart());
		assertEquals("2", history.get(0).getIdElement().getVersionIdPart());
		assertEquals(published, ResourceMetadataKeyEnum.PUBLISHED.get((IResource) history.get(1)));
		assertEquals(published, ResourceMetadataKeyEnum.PUBLISHED.get((IResource) history.get(1)));
		assertEquals(updated, ResourceMetadataKeyEnum.UPDATED.get((IResource) history.get(1)));
		assertEquals("001", ((Patient) history.get(1)).getIdentifierFirstRep().getValue());
		assertEquals(published2, ResourceMetadataKeyEnum.PUBLISHED.get( (IResource) history.get(0)));
		assertEquals(updated2, ResourceMetadataKeyEnum.UPDATED.get((IResource) history.get(0)));
		assertEquals("002", ((Patient) history.get(0)).getIdentifierFirstRep().getValue());

	}

	@Test
	public void testUpdateByUrl() {
		String methodName = "testUpdateByUrl";

		Patient p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue(methodName);
		IdDt id = ourPatientDao.create(p).getId();
		ourLog.info("Created patient, got it: {}", id);

		p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue(methodName);
		p.addName().addFamily("Hello");
		p.setId("Patient/" + methodName);

		ourPatientDao.update(p, "Patient?identifier=urn%3Asystem%7C" + methodName);

		p = ourPatientDao.read(id.toVersionless());
		assertThat(p.getId().toVersionless().toString(), not(containsString("test")));
		assertEquals(id.toVersionless(), p.getId().toVersionless());
		assertNotEquals(id, p.getId());
		assertThat(p.getId().toString(), endsWith("/_history/2"));

	}

	@Test
	public void testUpdateCreatesTextualIdIfItDoesntAlreadyExist() {
		Patient p = new Patient();
		String methodName = "testUpdateCreatesTextualIdIfItDoesntAlreadyExist";
		p.addIdentifier().setSystem("urn:system").setValue(methodName);
		p.addName().addFamily("Hello");
		p.setId("Patient/" + methodName);

		IdDt id = ourPatientDao.update(p).getId();
		assertEquals("Patient/" + methodName, id.toUnqualifiedVersionless().getValue());

		p = ourPatientDao.read(id);
		assertEquals(methodName, p.getIdentifierFirstRep().getValue());
	}

	@Test
	public void testUpdateFailsForUnknownIdWithNumberThenText() {
		String methodName = "testUpdateFailsForUnknownIdWithNumberThenText";
		Patient p = new Patient();
		p.setId("0" + methodName);
		p.addName().addFamily(methodName);

		try {
			ourPatientDao.update(p);
			fail();
		} catch (InvalidRequestException e) {
			assertThat(e.getMessage(), containsString("no resource with this ID exists and clients may only assign IDs which begin with a non-numeric character on this server"));
		}
	}

	@Test
	public void testUpdateMaintainsSearchParams() throws InterruptedException {
		Patient p1 = new Patient();
		p1.addIdentifier().setSystem("urn:system").setValue("testUpdateMaintainsSearchParamsDstu2AAA");
		p1.addName().addFamily("Tester").addGiven("testUpdateMaintainsSearchParamsDstu2AAA");
		IdDt p1id = ourPatientDao.create(p1).getId();

		Patient p2 = new Patient();
		p2.addIdentifier().setSystem("urn:system").setValue("testUpdateMaintainsSearchParamsDstu2BBB");
		p2.addName().addFamily("Tester").addGiven("testUpdateMaintainsSearchParamsDstu2BBB");
		ourPatientDao.create(p2).getId();

		Set<Long> ids = ourPatientDao.searchForIds(Patient.SP_GIVEN, new StringDt("testUpdateMaintainsSearchParamsDstu2AAA"));
		assertEquals(1, ids.size());
		assertThat(ids, contains(p1id.getIdPartAsLong()));

		// Update the name
		p1.getNameFirstRep().getGivenFirstRep().setValue("testUpdateMaintainsSearchParamsDstu2BBB");
		MethodOutcome update2 = ourPatientDao.update(p1);
		IdDt p1id2 = update2.getId();

		ids = ourPatientDao.searchForIds(Patient.SP_GIVEN, new StringDt("testUpdateMaintainsSearchParamsDstu2AAA"));
		assertEquals(0, ids.size());

		ids = ourPatientDao.searchForIds(Patient.SP_GIVEN, new StringDt("testUpdateMaintainsSearchParamsDstu2BBB"));
		assertEquals(2, ids.size());

		// Make sure vreads work
		p1 = ourPatientDao.read(p1id);
		assertEquals("testUpdateMaintainsSearchParamsDstu2AAA", p1.getNameFirstRep().getGivenAsSingleString());

		p1 = ourPatientDao.read(p1id2);
		assertEquals("testUpdateMaintainsSearchParamsDstu2BBB", p1.getNameFirstRep().getGivenAsSingleString());

	}

	@Test
	public void testUpdateRejectsInvalidTypes() throws InterruptedException {
		Patient p1 = new Patient();
		p1.addIdentifier().setSystem("urn:system").setValue("testUpdateRejectsInvalidTypes");
		p1.addName().addFamily("Tester").addGiven("testUpdateRejectsInvalidTypes");
		IdDt p1id = ourPatientDao.create(p1).getId();

		Organization p2 = new Organization();
		p2.getNameElement().setValue("testUpdateRejectsInvalidTypes");
		try {
			p2.setId(new IdDt("Organization/" + p1id.getIdPart()));
			ourOrganizationDao.update(p2);
			fail();
		} catch (UnprocessableEntityException e) {
			// good
		}

		try {
			p2.setId(new IdDt("Patient/" + p1id.getIdPart()));
			ourOrganizationDao.update(p2);
			fail();
		} catch (UnprocessableEntityException e) {
			// good
		}

	}

	@Test
	public void testUpdateUnknownNumericIdFails() {
		Patient p = new Patient();
		p.addIdentifier().setSystem("urn:system").setValue("testCreateNumericIdFails");
		p.addName().addFamily("Hello");
		p.setId("Patient/9999999999999999");
		try {
			ourPatientDao.update(p);
			fail();
		} catch (InvalidRequestException e) {
			assertThat(e.getMessage(), containsString("Can not create resource with ID[9999999999999999], no resource with this ID exists and clients may only"));
		}
	}

	@SuppressWarnings({ "rawtypes" })
	private List toList(IBundleProvider theSearch) {
		return theSearch.getResources(0, theSearch.size());
	}

	private List<IdDt> toUnqualifiedVersionlessIds(IBundleProvider theFound) {
		List<IdDt> retVal = new ArrayList<IdDt>();
		for (IBaseResource next : theFound.getResources(0, theFound.size())) {
			retVal.add((IdDt) next.getIdElement().toUnqualifiedVersionless());
		}
		return retVal;
	}

	@AfterClass
	public static void afterClass() {
		ourCtx.close();
	}

	@BeforeClass
	public static void beforeClass() {
		ourCtx = new ClassPathXmlApplicationContext("hapi-fhir-server-resourceproviders-dstu2.xml", "fhir-jpabase-spring-test-config.xml");
		ourPatientDao = ourCtx.getBean("myPatientDaoDstu2", IFhirResourceDao.class);
		ourObservationDao = ourCtx.getBean("myObservationDaoDstu2", IFhirResourceDao.class);
		ourDiagnosticReportDao = ourCtx.getBean("myDiagnosticReportDaoDstu2", IFhirResourceDao.class);
		ourDeviceDao = ourCtx.getBean("myDeviceDaoDstu2", IFhirResourceDao.class);
		ourOrganizationDao = ourCtx.getBean("myOrganizationDaoDstu2", IFhirResourceDao.class);
		ourLocationDao = ourCtx.getBean("myLocationDaoDstu2", IFhirResourceDao.class);
		ourEncounterDao = ourCtx.getBean("myEncounterDaoDstu2", IFhirResourceDao.class);
		ourSystemDao = ourCtx.getBean("mySystemDaoDstu2", IFhirSystemDao.class);
		ourFhirCtx = ourCtx.getBean(FhirContext.class);
	}

	private static void deleteEverything() {
		FhirSystemDaoDstu2Test.doDeleteEverything(ourSystemDao);
	}

	@Test
	public void testSearchWithNoResults() {
		Device dev = new Device();
		dev.addIdentifier().setSystem("Foo");
		ourDeviceDao.create(dev);

		IBundleProvider value = ourDeviceDao.search(new SearchParameterMap());
		ourLog.info("Initial size: " + value.size());
		for (IBaseResource next : value.getResources(0, value.size())) {
			ourLog.info("Deleting: {}", next.getIdElement());
			ourDeviceDao.delete((IdDt) next.getIdElement());
		}

		value = ourDeviceDao.search(new SearchParameterMap());
		if (value.size() > 0) {
			ourLog.info("Found: " + (value.getResources(0, 1).get(0).getIdElement()));
			fail(ourFhirCtx.newXmlParser().setPrettyPrint(true).encodeResourceToString(value.getResources(0, 1).get(0)));
		}
		assertEquals(0, value.size());

		List<IBaseResource> res = value.getResources(0, 0);
		assertTrue(res.isEmpty());

	}

}
