/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.peter.ivarsson.rest.doc.test;

import java.io.File;

import org.apache.maven.plugin.testing.AbstractMojoTestCase;

import se.peter.ivarsson.rest.doc.RESTDocMojo;


/**
 *
 * @author Peter Ivarsson     Peter.Ivarsson@cybercom.com
 */
public class RESTDocMojoTest extends AbstractMojoTestCase {
   
   @Override
   protected void setUp() throws Exception
   {
      // required
      super.setUp();
   }

   @Override
   protected void tearDown() throws Exception
   {
       // required
       super.tearDown();
   }

   /**
    * @throws Exception if any is wrong
    */
   public void testRESTDocParameters() throws Exception
   {
      File pom = getTestFile( "pom.xml" );
      assertNotNull( pom );
      assertTrue( pom.exists() );

      RESTDocMojo myRESTDocMojo = (RESTDocMojo) lookupMojo( "restdoc", pom );
      assertNotNull( myRESTDocMojo );
      myRESTDocMojo.execute();
   }
}

