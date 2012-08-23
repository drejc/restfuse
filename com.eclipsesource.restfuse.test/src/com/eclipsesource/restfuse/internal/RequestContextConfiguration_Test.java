/*******************************************************************************
 * Copyright (c) 2011 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Holger Staudacher - initial API and implementation
 ******************************************************************************/ 
package com.eclipsesource.restfuse.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import junit.framework.TestResult;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;

import com.eclipsesource.restfuse.AuthenticationType;
import com.eclipsesource.restfuse.DefaultCallbackResource;
import com.eclipsesource.restfuse.Destination;
import com.eclipsesource.restfuse.MediaType;
import com.eclipsesource.restfuse.Method;
import com.eclipsesource.restfuse.Request;
import com.eclipsesource.restfuse.Response;
import com.eclipsesource.restfuse.annotation.Authentication;
import com.eclipsesource.restfuse.annotation.Header;
import com.eclipsesource.restfuse.annotation.HttpTest;
import com.eclipsesource.restfuse.internal.callback.CallbackSerlvet;
import com.eclipsesource.restfuse.internal.callback.CallbackStatement;


public class RequestContextConfiguration_Test {
  

private static final int TIMEOUT = 10;

  private static Server server;
  
  private static TestResource resource = new TestResource();
  
  @BeforeClass
  public static void setUp() throws Exception {
    server = new Server( 10045 );
    Context context = new Context( server, "/", Context.SESSIONS );
    CallbackStatement statement = mock( CallbackStatement.class );
    
    CallbackSerlvet servlet = new CallbackSerlvet( resource, statement );
    context.addServlet( new ServletHolder( servlet ), "/" );
    server.start();
    int timer = 0;
    while( !server.isRunning() && timer < TIMEOUT ) {
      Thread.sleep( 1000 );
      timer++;
    }
  }
  
  @AfterClass
  public static void tearDown() throws Exception {
    server.stop();
    int timer = 0;
    while( !server.isStopped() && timer < TIMEOUT ) {
      Thread.sleep( 1000 );
      timer++;
    }
  }
  
  private static class TestResource extends DefaultCallbackResource {
    
	public Request request;
	  
	@Override
	public Response post( Request request ) {
  	  // store request to be used in test case
	  this.request = request;
      return super.post( request );
    }    
  }
  
  @Rule
  public Destination destination = getDestination();  
    
  private Destination getDestination() {
	  Destination destination = new Destination( "http://localhost:10045/test" );
	  destination.getRequestContext().headers.put("test", "value");
	  return destination;
  }
  
  @Test
  @HttpTest( 
    method = Method.POST,
    path = "/",
    authentications = { @Authentication( type = AuthenticationType.BASIC, user = "test", password = "pass" ) },
    type = MediaType.TEXT_PLAIN,
    content = "test"
  )
  public void fakeTestMethod() {	
      assertEquals( "test", resource.request.getBody() );
      Map<String, List<String>> headers = resource.request.getHeaders();
      assertTrue( headers.containsKey( "Authorization" ) );
      assertEquals( "value", headers.get( "test" ).get( 0 ) );
      assertEquals( MediaType.TEXT_PLAIN, resource.request.getType() );
  }

}
