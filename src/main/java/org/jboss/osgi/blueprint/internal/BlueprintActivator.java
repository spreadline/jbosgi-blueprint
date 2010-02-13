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
package org.jboss.osgi.blueprint.internal;

//$Id$

import org.apache.aries.blueprint.container.BlueprintExtender;
import org.jboss.logging.Logger;
import org.jboss.osgi.blueprint.BlueprintService;
import org.jboss.osgi.deployment.interceptor.LifecycleInterceptorService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The Blueprint activator registeres the {@link BlueprintService} 
 * 
 * @author thomas.diesler@jboss.com
 * @since 13-May-2009
 */
public class BlueprintActivator implements BundleActivator
{
   // Provide logging
   private static final Logger log = Logger.getLogger(BlueprintActivator.class);
   
   private BundleActivator ariesActivator;
   private BlueprintInterceptor jbossInterceptor;
   
   public void start(BundleContext context) throws Exception
   {
      // Register the marker service
      BlueprintService service = new BlueprintService(){};
      context.registerService(BlueprintService.class.getName(), service, null);
      
      ServiceReference sref = context.getServiceReference(LifecycleInterceptorService.class.getName());
      if (sref != null)
      {
         log.debug("Start: " + BlueprintInterceptor.class.getName());
         jbossInterceptor = new BlueprintInterceptor();
         jbossInterceptor.start(context);
      }
      else
      {
         log.debug("Start: " + BlueprintExtender.class.getName());
         ariesActivator = new BlueprintExtender();
         ariesActivator.start(context);
      }
   }

   public void stop(BundleContext context) throws Exception
   {
      if (ariesActivator != null)
      {
         log.debug("Stop: " + ariesActivator.getClass().getName());
         ariesActivator.stop(context);
      }
      else if (jbossInterceptor != null)
      {
         log.debug("Stop: " + jbossInterceptor.getClass().getName());
         jbossInterceptor.stop(context);
      }
   }
}