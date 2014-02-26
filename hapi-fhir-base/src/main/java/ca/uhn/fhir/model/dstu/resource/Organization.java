











package ca.uhn.fhir.model.dstu.resource;

import java.util.*;
import ca.uhn.fhir.model.api.*;
import ca.uhn.fhir.model.api.annotation.*;
import ca.uhn.fhir.model.primitive.*;
import ca.uhn.fhir.model.dstu.composite.*;

/**
 * HAPI/FHIR <b>Organization</b> Resource
 * (A grouping of people or organizations with a common purpose)
 *
 * <p>
 * <b>Definition:</b>
 * A formally or informally recognized grouping of people or organizations formed for the purpose of achieving some form of collective action.  Includes companies, institutions, corporations, departments, community groups, healthcare practice groups, etc
 * </p> 
 *
 * <p>
 * <b>Requirements:</b>
 * 
 * </p> 
 */
@ResourceDef(name="Organization")
public class Organization implements IResource {

	@Child(name="identifier", type=IdentifierDt.class, order=0, min=0, max=Child.MAX_UNLIMITED)	
	private List<IdentifierDt> myIdentifier;
	
	@Child(name="name", type=StringDt.class, order=1, min=0, max=1)	
	private StringDt myName;
	
	@Child(name="type", type=CodeableConceptDt.class, order=2, min=0, max=1)	
	private CodeableConceptDt myType;
	
	@Child(name="telecom", type=ContactDt.class, order=3, min=0, max=Child.MAX_UNLIMITED)	
	private List<ContactDt> myTelecom;
	
	@Child(name="address", type=AddressDt.class, order=4, min=0, max=Child.MAX_UNLIMITED)	
	private List<AddressDt> myAddress;
	
	@Child(name="partOf", order=5, min=0, max=1)
	@ChildResource(types= {
		Organization.class,
	})	
	private ResourceReference myPartOf;
	
	@Child(name="contact", order=6, min=0, max=Child.MAX_UNLIMITED)	
	private List<Contact> myContact;
	
	@Child(name="location", order=7, min=0, max=Child.MAX_UNLIMITED)
	@ChildResource(types= {
		Location.class,
	})	
	private List<ResourceReference> myLocation;
	
	@Child(name="active", type=BooleanDt.class, order=8, min=0, max=1)	
	private BooleanDt myActive;
	
	/**
	 * Gets the value(s) for <b>identifier</b> (Identifies this organization  across multiple systems).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * Identifier for the organization that is used to identify the organization across multiple disparate systems
     * </p> 
	 */
	public List<IdentifierDt> getIdentifier() {  
		if (myIdentifier == null) {
			myIdentifier = new ArrayList<IdentifierDt>();
		}
		return myIdentifier;
	}

	/**
	 * Sets the value(s) for <b>identifier</b> (Identifies this organization  across multiple systems)
	 *
     * <p>
     * <b>Definition:</b>
     * Identifier for the organization that is used to identify the organization across multiple disparate systems
     * </p> 
	 */
	public void setIdentifier(List<IdentifierDt> theValue) {
		myIdentifier = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>name</b> (Name used for the organization).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * A name associated with the organization
     * </p> 
	 */
	public StringDt getName() {  
		if (myName == null) {
			myName = new StringDt();
		}
		return myName;
	}

	/**
	 * Sets the value(s) for <b>name</b> (Name used for the organization)
	 *
     * <p>
     * <b>Definition:</b>
     * A name associated with the organization
     * </p> 
	 */
	public void setName(StringDt theValue) {
		myName = theValue;
	}
	
	/**
	 * Sets the value(s) for <b>name</b> (Name used for the organization)
	 *
     * <p>
     * <b>Definition:</b>
     * A name associated with the organization
     * </p> 
	 */
	public void setName( String theString) {
		myName = new StringDt(theString); 
	}
 
	/**
	 * Gets the value(s) for <b>type</b> (Kind of organization).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * The kind of organization that this is
     * </p> 
	 */
	public CodeableConceptDt getType() {  
		if (myType == null) {
			myType = new CodeableConceptDt();
		}
		return myType;
	}

	/**
	 * Sets the value(s) for <b>type</b> (Kind of organization)
	 *
     * <p>
     * <b>Definition:</b>
     * The kind of organization that this is
     * </p> 
	 */
	public void setType(CodeableConceptDt theValue) {
		myType = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>telecom</b> (A contact detail for the organization).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * A contact detail for the organization
     * </p> 
	 */
	public List<ContactDt> getTelecom() {  
		if (myTelecom == null) {
			myTelecom = new ArrayList<ContactDt>();
		}
		return myTelecom;
	}

	/**
	 * Sets the value(s) for <b>telecom</b> (A contact detail for the organization)
	 *
     * <p>
     * <b>Definition:</b>
     * A contact detail for the organization
     * </p> 
	 */
	public void setTelecom(List<ContactDt> theValue) {
		myTelecom = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>address</b> (An address for the organization).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * An address for the organization
     * </p> 
	 */
	public List<AddressDt> getAddress() {  
		if (myAddress == null) {
			myAddress = new ArrayList<AddressDt>();
		}
		return myAddress;
	}

	/**
	 * Sets the value(s) for <b>address</b> (An address for the organization)
	 *
     * <p>
     * <b>Definition:</b>
     * An address for the organization
     * </p> 
	 */
	public void setAddress(List<AddressDt> theValue) {
		myAddress = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>partOf</b> (The organization of which this organization forms a part).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * The organization of which this organization forms a part
     * </p> 
	 */
	public ResourceReference getPartOf() {  
		if (myPartOf == null) {
			myPartOf = new ResourceReference();
		}
		return myPartOf;
	}

	/**
	 * Sets the value(s) for <b>partOf</b> (The organization of which this organization forms a part)
	 *
     * <p>
     * <b>Definition:</b>
     * The organization of which this organization forms a part
     * </p> 
	 */
	public void setPartOf(ResourceReference theValue) {
		myPartOf = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>contact</b> (Contact for the organization for a certain purpose).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * 
     * </p> 
	 */
	public List<Contact> getContact() {  
		if (myContact == null) {
			myContact = new ArrayList<Contact>();
		}
		return myContact;
	}

	/**
	 * Sets the value(s) for <b>contact</b> (Contact for the organization for a certain purpose)
	 *
     * <p>
     * <b>Definition:</b>
     * 
     * </p> 
	 */
	public void setContact(List<Contact> theValue) {
		myContact = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>location</b> (Location(s) the organization uses to provide services).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * Location(s) the organization uses to provide services
     * </p> 
	 */
	public List<ResourceReference> getLocation() {  
		if (myLocation == null) {
			myLocation = new ArrayList<ResourceReference>();
		}
		return myLocation;
	}

	/**
	 * Sets the value(s) for <b>location</b> (Location(s) the organization uses to provide services)
	 *
     * <p>
     * <b>Definition:</b>
     * Location(s) the organization uses to provide services
     * </p> 
	 */
	public void setLocation(List<ResourceReference> theValue) {
		myLocation = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>active</b> (Whether the organization's record is still in active use).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * Whether the organization's record is still in active use
     * </p> 
	 */
	public BooleanDt getActive() {  
		if (myActive == null) {
			myActive = new BooleanDt();
		}
		return myActive;
	}

	/**
	 * Sets the value(s) for <b>active</b> (Whether the organization's record is still in active use)
	 *
     * <p>
     * <b>Definition:</b>
     * Whether the organization's record is still in active use
     * </p> 
	 */
	public void setActive(BooleanDt theValue) {
		myActive = theValue;
	}
	
 

	/**
	 * Block class for child element: <b>Organization.contact</b> (Contact for the organization for a certain purpose)
	 *
     * <p>
     * <b>Definition:</b>
     * 
     * </p> 
	 */
	@Block(name="Organization.contact")	
	public static class Contact implements IResourceBlock {
	@Child(name="identifier", type=IdentifierDt.class, order=0, min=0, max=Child.MAX_UNLIMITED)	
	private List<IdentifierDt> myIdentifier;
	
	@Child(name="name", type=StringDt.class, order=1, min=0, max=1)	
	private StringDt myName;
	
	@Child(name="type", type=CodeableConceptDt.class, order=2, min=0, max=1)	
	private CodeableConceptDt myType;
	
	@Child(name="telecom", type=ContactDt.class, order=3, min=0, max=Child.MAX_UNLIMITED)	
	private List<ContactDt> myTelecom;
	
	@Child(name="address", type=AddressDt.class, order=4, min=0, max=Child.MAX_UNLIMITED)	
	private List<AddressDt> myAddress;
	
	@Child(name="partOf", order=5, min=0, max=1)
	@ChildResource(types= {
		Organization.class,
	})	
	private ResourceReference myPartOf;
	
	@Child(name="contact", order=6, min=0, max=Child.MAX_UNLIMITED)	
	private List<Contact> myContact;
	
	@Child(name="location", order=7, min=0, max=Child.MAX_UNLIMITED)
	@ChildResource(types= {
		Location.class,
	})	
	private List<ResourceReference> myLocation;
	
	@Child(name="active", type=BooleanDt.class, order=8, min=0, max=1)	
	private BooleanDt myActive;
	
	/**
	 * Gets the value(s) for <b>identifier</b> (Identifies this organization  across multiple systems).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * Identifier for the organization that is used to identify the organization across multiple disparate systems
     * </p> 
	 */
	public List<IdentifierDt> getIdentifier() {  
		if (myIdentifier == null) {
			myIdentifier = new ArrayList<IdentifierDt>();
		}
		return myIdentifier;
	}

	/**
	 * Sets the value(s) for <b>identifier</b> (Identifies this organization  across multiple systems)
	 *
     * <p>
     * <b>Definition:</b>
     * Identifier for the organization that is used to identify the organization across multiple disparate systems
     * </p> 
	 */
	public void setIdentifier(List<IdentifierDt> theValue) {
		myIdentifier = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>name</b> (Name used for the organization).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * A name associated with the organization
     * </p> 
	 */
	public StringDt getName() {  
		if (myName == null) {
			myName = new StringDt();
		}
		return myName;
	}

	/**
	 * Sets the value(s) for <b>name</b> (Name used for the organization)
	 *
     * <p>
     * <b>Definition:</b>
     * A name associated with the organization
     * </p> 
	 */
	public void setName(StringDt theValue) {
		myName = theValue;
	}
	
	/**
	 * Sets the value(s) for <b>name</b> (Name used for the organization)
	 *
     * <p>
     * <b>Definition:</b>
     * A name associated with the organization
     * </p> 
	 */
	public void setName( String theString) {
		myName = new StringDt(theString); 
	}
 
	/**
	 * Gets the value(s) for <b>type</b> (Kind of organization).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * The kind of organization that this is
     * </p> 
	 */
	public CodeableConceptDt getType() {  
		if (myType == null) {
			myType = new CodeableConceptDt();
		}
		return myType;
	}

	/**
	 * Sets the value(s) for <b>type</b> (Kind of organization)
	 *
     * <p>
     * <b>Definition:</b>
     * The kind of organization that this is
     * </p> 
	 */
	public void setType(CodeableConceptDt theValue) {
		myType = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>telecom</b> (A contact detail for the organization).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * A contact detail for the organization
     * </p> 
	 */
	public List<ContactDt> getTelecom() {  
		if (myTelecom == null) {
			myTelecom = new ArrayList<ContactDt>();
		}
		return myTelecom;
	}

	/**
	 * Sets the value(s) for <b>telecom</b> (A contact detail for the organization)
	 *
     * <p>
     * <b>Definition:</b>
     * A contact detail for the organization
     * </p> 
	 */
	public void setTelecom(List<ContactDt> theValue) {
		myTelecom = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>address</b> (An address for the organization).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * An address for the organization
     * </p> 
	 */
	public List<AddressDt> getAddress() {  
		if (myAddress == null) {
			myAddress = new ArrayList<AddressDt>();
		}
		return myAddress;
	}

	/**
	 * Sets the value(s) for <b>address</b> (An address for the organization)
	 *
     * <p>
     * <b>Definition:</b>
     * An address for the organization
     * </p> 
	 */
	public void setAddress(List<AddressDt> theValue) {
		myAddress = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>partOf</b> (The organization of which this organization forms a part).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * The organization of which this organization forms a part
     * </p> 
	 */
	public ResourceReference getPartOf() {  
		if (myPartOf == null) {
			myPartOf = new ResourceReference();
		}
		return myPartOf;
	}

	/**
	 * Sets the value(s) for <b>partOf</b> (The organization of which this organization forms a part)
	 *
     * <p>
     * <b>Definition:</b>
     * The organization of which this organization forms a part
     * </p> 
	 */
	public void setPartOf(ResourceReference theValue) {
		myPartOf = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>contact</b> (Contact for the organization for a certain purpose).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * 
     * </p> 
	 */
	public List<Contact> getContact() {  
		if (myContact == null) {
			myContact = new ArrayList<Contact>();
		}
		return myContact;
	}

	/**
	 * Sets the value(s) for <b>contact</b> (Contact for the organization for a certain purpose)
	 *
     * <p>
     * <b>Definition:</b>
     * 
     * </p> 
	 */
	public void setContact(List<Contact> theValue) {
		myContact = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>location</b> (Location(s) the organization uses to provide services).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * Location(s) the organization uses to provide services
     * </p> 
	 */
	public List<ResourceReference> getLocation() {  
		if (myLocation == null) {
			myLocation = new ArrayList<ResourceReference>();
		}
		return myLocation;
	}

	/**
	 * Sets the value(s) for <b>location</b> (Location(s) the organization uses to provide services)
	 *
     * <p>
     * <b>Definition:</b>
     * Location(s) the organization uses to provide services
     * </p> 
	 */
	public void setLocation(List<ResourceReference> theValue) {
		myLocation = theValue;
	}
	
 
	/**
	 * Gets the value(s) for <b>active</b> (Whether the organization's record is still in active use).
	 * creating it if it does
	 * not exist. Will not return <code>null</code>.
	 *
     * <p>
     * <b>Definition:</b>
     * Whether the organization's record is still in active use
     * </p> 
	 */
	public BooleanDt getActive() {  
		if (myActive == null) {
			myActive = new BooleanDt();
		}
		return myActive;
	}

	/**
	 * Sets the value(s) for <b>active</b> (Whether the organization's record is still in active use)
	 *
     * <p>
     * <b>Definition:</b>
     * Whether the organization's record is still in active use
     * </p> 
	 */
	public void setActive(BooleanDt theValue) {
		myActive = theValue;
	}
	
 
	}



}