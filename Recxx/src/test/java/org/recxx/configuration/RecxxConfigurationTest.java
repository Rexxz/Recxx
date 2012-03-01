package org.recxx.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import java.util.Properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Test;

public class RecxxConfigurationTest {

	//TODO Rewrite this
	
	private static final String _01_APR_2011 = "01-Apr-2011";
	private static final String FILE_NAME = "Moose";
	private static final String TEST_PROPERTY = "testProperty";
	private static final String TEST_VALUE = "testValue";
	private static final String TEST_PROPERTY_PLUS_DATE_PROPERTY = "testPropertyPlusDateProperty";
	private static final String TEST_PROPERTY_PLUS_DATE_PROPERTY_AND_OTHER_PROPERTY = "testPropertyPlusDatePropertyAndOtherProperty";
	private static final String TEST_VALUE_PLUS_DATE_PROPERTY = "testValue${BUSINESS_DATE}";
	private static final String TEST_VALUE_PLUS_DATE_PROPERTY_AND_OTHER_PROPERTY = "testValue${BUSINESS_DATE}testValue${FILE_NAME}";
	private static final String TEST_PROPERTY_PLUS_NOT_SET_PROPERTY = "testPropertyPlusNotSetProperty";
	private static final String TEST_VALUE_PLUS_NOT_SET_PROPERTY = "testValue${NOT_SET_RPOPERTY}";
	
	private Properties props;
	private PropertiesConfiguration propertiesConfigurtation;
	private RecxxConfiguration recxxConfiguration;
	{
		props = new Properties();
		props.put(TEST_PROPERTY, TEST_VALUE);
		props.put(TEST_PROPERTY_PLUS_DATE_PROPERTY, TEST_VALUE_PLUS_DATE_PROPERTY);
		props.put(TEST_PROPERTY_PLUS_DATE_PROPERTY_AND_OTHER_PROPERTY, TEST_VALUE_PLUS_DATE_PROPERTY_AND_OTHER_PROPERTY);
		props.put(TEST_PROPERTY_PLUS_NOT_SET_PROPERTY, TEST_VALUE_PLUS_NOT_SET_PROPERTY);
		System.setProperty("BUSINESS_DATE", _01_APR_2011);
		System.setProperty("FILE_NAME", FILE_NAME);
		propertiesConfigurtation = new PropertiesConfiguration();
		for (Object key : props.keySet()) {
			propertiesConfigurtation.setProperty(key.toString(), props.get(key));			
		}
		recxxConfiguration = new RecxxConfiguration(propertiesConfigurtation);
	}
	
	@Test
	public void testListConfig() throws ConfigurationException {
		String[] strings = new String[]{"dd-MMM-yyyy","dd-MM-yyyy"};
		propertiesConfigurtation.addProperty("dateFormat", strings);
		recxxConfiguration = new RecxxConfiguration(propertiesConfigurtation);
		assertArrayEquals(recxxConfiguration.getStringArray("dateFormat"), strings);
	}

	@Test
	public void testGetProperty() {
		assertEquals("Properties values should be the same!", recxxConfiguration.getString(TEST_PROPERTY), TEST_VALUE);
	}
	
	@Test
	public void testGetPropertyWithProperty() {
		assertEquals("Properties values should be the same!", recxxConfiguration.getString(TEST_PROPERTY_PLUS_DATE_PROPERTY), TEST_VALUE + _01_APR_2011);
	}

	@Test
	public void testGetPropertyWithTwoProperties() {
		assertEquals("Properties values should be the same!", recxxConfiguration.getString(TEST_PROPERTY_PLUS_DATE_PROPERTY_AND_OTHER_PROPERTY), TEST_VALUE + _01_APR_2011 + TEST_VALUE + FILE_NAME);
	}

	@Test
	public void testGetPropertyWithUnsetProperty() {
		assertNull(recxxConfiguration.getString("UnsetProperty"));
	}
	
	@Test
	public void testGetPropertyWithUnsetConfiguredProperty() {
		assertEquals("Properties values should be the same!", recxxConfiguration.getString(TEST_PROPERTY_PLUS_NOT_SET_PROPERTY), TEST_VALUE_PLUS_NOT_SET_PROPERTY);
	}
	
	

}
