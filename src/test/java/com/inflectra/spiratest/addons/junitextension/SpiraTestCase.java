package com.inflectra.spiratest.addons.junitextension;

import java.lang.annotation.*;

/**
 * This defines the 'SpiraTestCase' annotation used to specify the
 * SpiraTest test case that the JUnit test maps to
 * 
 * @author		Inflectra Corporation
 * @version		4.0.0
 *
 */
@Retention(value= RetentionPolicy.RUNTIME)
@Target(value= ElementType.METHOD)
public @interface SpiraTestCase
{
	int testCaseId();
}