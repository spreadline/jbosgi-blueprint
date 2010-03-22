/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.osgi.blueprint.container;

//$Id$

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeNotNull;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServer;

import org.jboss.osgi.blueprint.BlueprintCapability;
import org.jboss.osgi.husky.Bridge;
import org.jboss.osgi.husky.BridgeFactory;
import org.jboss.osgi.husky.HuskyCapability;
import org.jboss.osgi.husky.RuntimeContext;
import org.jboss.osgi.jmx.JMXCapability;
import org.jboss.osgi.spi.capability.LogServiceCapability;
import org.jboss.osgi.testing.OSGiBundle;
import org.jboss.osgi.testing.OSGiRuntime;
import org.jboss.osgi.testing.OSGiRuntimeHelper;
import org.jboss.test.osgi.blueprint.container.bundle.BeanA;
import org.jboss.test.osgi.blueprint.container.bundle.BeanB;
import org.jboss.test.osgi.blueprint.container.bundle.ServiceA;
import org.jboss.test.osgi.blueprint.container.bundle.ServiceB;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.blueprint.container.BlueprintContainer;
import org.osgi.service.blueprint.reflect.BeanMetadata;
import org.osgi.service.blueprint.reflect.ComponentMetadata;
import org.osgi.service.blueprint.reflect.ReferenceMetadata;
import org.osgi.service.blueprint.reflect.ServiceMetadata;

/**
 * BlueprintContainer API tests
 * 
 * @author thomas.diesler@jboss.com
 * @since 13-May-2009
 */
public class BlueprintContainerTestCase
{
   @RuntimeContext
   public static BundleContext context;

   private static OSGiRuntime runtime;
   private static Bridge huskyBridge;

   @BeforeClass
   public static void beforeClass() throws Exception
   {
      if (context == null)
      {
         runtime = new OSGiRuntimeHelper().getEmbeddedRuntime();
         runtime.addCapability(new HuskyCapability());
         runtime.addCapability(new LogServiceCapability());
         runtime.addCapability(new JMXCapability());
         runtime.addCapability(new BlueprintCapability());

         huskyBridge = BridgeFactory.getBridge();

         OSGiBundle bundle = runtime.installBundle("container-basic.jar");
         bundle.start();
      }
   }

   @AfterClass
   public static void afterClass() throws Exception
   {
      if (context == null)
         runtime.shutdown();
   }

   @Test
   public void testBlueprintBundleInstall() throws Exception
   {
      if (context == null)
         huskyBridge.run();

      assumeNotNull(context);

      Bundle bundle = context.getBundle();
      assertEquals("container-basic", bundle.getSymbolicName());
   }

   @Test
   public void testBlueprintContainerAvailable() throws Exception
   {
      if (context == null)
         huskyBridge.run();

      assumeNotNull(context);

      BlueprintContainer bpContainer = getBlueprintContainer();
      assertNotNull("BlueprintContainer available", bpContainer);
   }

   @Test
   public void getComponent() throws Exception
   {
      if (context == null)
         huskyBridge.run();

      assumeNotNull(context);

      BlueprintContainer bpContainer = getBlueprintContainer();
      BeanA beanA = (BeanA)bpContainer.getComponentInstance("beanA");
      assertNotNull("ComponentInstance available", beanA);
   }

   @Test
   public void getService() throws Exception
   {
      if (context == null)
         huskyBridge.run();

      assumeNotNull(context);

      ServiceReference srefA = context.getServiceReference(ServiceA.class.getName());
      assertNotNull("ServiceReference available", srefA);

      ServiceA serviceA = (ServiceA)context.getService(srefA);
      assertNotNull("Service available", serviceA);

      ServiceReference srefB = context.getServiceReference(ServiceB.class.getName());
      assertNotNull("ServiceReference available", srefB);

      ServiceB serviceB = (ServiceB)context.getService(srefB);
      assertNotNull("Service available", serviceB);

      BeanB beanB = (BeanB)serviceB;
      BeanA beanA = beanB.getBeanA();
      assertNotNull("BeanA available", beanA);

      MBeanServer mbeanServer = beanA.getMbeanServer();
      assertNotNull("MBeanServer available", mbeanServer);
   }

   @Test
   public void testComponentMetadataByName() throws Exception
   {
      if (context == null)
         huskyBridge.run();

      assumeNotNull(context);

      BlueprintContainer bpContainer = getBlueprintContainer();
      ComponentMetadata compMetadata = bpContainer.getComponentMetadata("beanA");

      assertNotNull("ComponentMetadata not null", compMetadata);
      assertEquals("beanA", compMetadata.getId());
   }

   @Test
   @SuppressWarnings("unchecked")
   public void getComponentIds() throws Exception
   {
      if (context == null)
         huskyBridge.run();

      assumeNotNull(context);

      BlueprintContainer bpContainer = getBlueprintContainer();
      Set<String> compNames = bpContainer.getComponentIds();

      assertNotNull("ComponentNames not null", compNames);
      assertEquals("ComponentNames size", 8, compNames.size());
      assertTrue("ComponentNames contains beanA", compNames.contains("beanA"));
      assertTrue("ComponentNames contains serviceA", compNames.contains("serviceA"));
      assertTrue("ComponentNames contains serviceB", compNames.contains("serviceB"));
      assertTrue("ComponentNames contains mbeanService", compNames.contains("mbeanService"));
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testBeanMetadata() throws Exception
   {
      if (context == null)
         huskyBridge.run();

      assumeNotNull(context);

      BlueprintContainer bpContainer = getBlueprintContainer();
      Collection<BeanMetadata> bcMetadata = bpContainer.getMetadata(BeanMetadata.class);

      assertNotNull("BeanComponentsMetadata not null", bcMetadata);
      assertEquals("BeanComponentsMetadata size", 2, bcMetadata.size());

      BeanMetadata bmd = bcMetadata.iterator().next();
      assertEquals("beanA", bmd.getId());
      assertEquals(BeanA.class.getName(), bmd.getClassName());
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testServiceMetadata() throws Exception
   {
      if (context == null)
         huskyBridge.run();

      assumeNotNull(context);

      BlueprintContainer bpContainer = getBlueprintContainer();
      Collection<ServiceMetadata> servicesMetadata = bpContainer.getMetadata(ServiceMetadata.class);

      assertNotNull("ServiceMetadata not null", servicesMetadata);
      assertEquals("ServiceMetadata size", 2, servicesMetadata.size());

      Iterator<ServiceMetadata> itServices = servicesMetadata.iterator();
      ServiceMetadata serviceA = itServices.next();
      assertEquals("serviceA", serviceA.getId());

      List<String> interfaceNamesA = serviceA.getInterfaces();
      assertNotNull("InterfaceNames not null", interfaceNamesA);
      assertEquals("InterfaceNames size", 1, interfaceNamesA.size());
      assertEquals("InterfaceName", ServiceA.class.getName(), interfaceNamesA.get(0));

      ServiceMetadata serviceB = itServices.next();
      assertEquals("serviceB", serviceB.getId());

      List<String> interfaceNamesB = serviceB.getInterfaces();
      assertNotNull("InterfaceNames not null", interfaceNamesB);
      assertEquals("InterfaceNames size", 1, interfaceNamesB.size());
      assertEquals("InterfaceName", ServiceB.class.getName(), interfaceNamesB.get(0));
   }

   @Test
   @SuppressWarnings("unchecked")
   public void testServiceReferenceMetadata() throws Exception
   {
      if (context == null)
         huskyBridge.run();

      assumeNotNull(context);

      BlueprintContainer bpContainer = getBlueprintContainer();
      Collection<ReferenceMetadata> srefsMetadata = bpContainer.getMetadata(ReferenceMetadata.class);

      assertNotNull("ReferenceMetadata not null", srefsMetadata);
      assertEquals("ReferenceMetadata size", 1, srefsMetadata.size());

      ReferenceMetadata srefMetadata = srefsMetadata.iterator().next();
      assertEquals("mbeanService", srefMetadata.getId());

      String interfaceName = srefMetadata.getInterface();
      assertNotNull("InterfaceName not null", interfaceName);
      assertEquals("InterfaceName", MBeanServer.class.getName(), interfaceName);
   }

   private BlueprintContainer getBlueprintContainer() throws Exception
   {
      // 10sec for processing of STARTING event
      int timeout = 50;

      ServiceReference sref = null;
      while (sref == null && 0 < timeout--)
      {
         sref = context.getServiceReference(BlueprintContainer.class.getName());
         Thread.sleep(200);
      }
      assertNotNull("BlueprintContainer not null", sref);

      BlueprintContainer bpContainer = (BlueprintContainer)context.getService(sref);
      return bpContainer;
   }
}