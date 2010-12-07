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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.aries.blueprint.BlueprintConstants;
import org.apache.aries.blueprint.container.BlueprintContainerImpl;
import org.apache.aries.blueprint.container.NamespaceHandlerRegistry;
import org.apache.aries.blueprint.namespace.NamespaceHandlerRegistryImpl;
import org.jboss.osgi.deployment.interceptor.AbstractLifecycleInterceptor;
import org.jboss.osgi.deployment.interceptor.InvocationContext;
import org.jboss.osgi.deployment.interceptor.LifecycleInterceptor;
import org.jboss.osgi.deployment.interceptor.LifecycleInterceptorException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Blueprint interceptor
 * 
 * @author thomas.diesler@jboss.com
 * @since 20-Oct-2009
 */
public class BlueprintInterceptor extends AbstractLifecycleInterceptor implements LifecycleInterceptor
{
   // Provide logging
   private Logger log = LoggerFactory.getLogger(BlueprintInterceptor.class);
   
   private Map<Bundle, BlueprintContainerImpl> containers;
   private BlueprintEventDispatcher eventDispatcher;
   private NamespaceHandlerRegistry handlers;
   private ScheduledExecutorService executors;
   
   public void start(BundleContext context)
   {
      containers = new HashMap<Bundle, BlueprintContainerImpl>();
      handlers = new NamespaceHandlerRegistryImpl(context);
      executors = Executors.newScheduledThreadPool(3);
      eventDispatcher = new BlueprintEventDispatcher(context, executors);
      
      context.registerService(LifecycleInterceptor.class.getName(), this, null);
   }
   
   public void stop(BundleContext context)
   {
      for (Bundle bundle : containers.keySet())
      {
         BlueprintContainerImpl blueprintContainer = containers.remove(bundle);
         blueprintContainer.destroy();
      }
   }

   public void invoke(int state, InvocationContext context) throws LifecycleInterceptorException
   {
      Bundle bundle = context.getBundle();
      if (state == Bundle.STARTING)
      {
         // If a Bundle-Blueprint manifest header is defined, then this header contains a list of paths. 
         // If this header is not defined, then resources ending in .xml in the bundle’s
         // OSGI-INF/blueprint directory must be used. These are the resources that
         // would be found by calling the Bundle findEntries("OSGI-INF/blueprint", "*.xml", false) method.

         List<Object> pathList = new ArrayList<Object>();

         String descriptorPaths = (String)bundle.getHeaders().get(BlueprintConstants.BUNDLE_BLUEPRINT_HEADER);
         if (descriptorPaths != null)
         {
            StringTokenizer tokenizer = new StringTokenizer(descriptorPaths, ",");
            while (tokenizer.hasMoreTokens())
            {
               String path = tokenizer.nextToken();
               pathList.add(path.trim());
            }
         }
         else
         {
            Enumeration<?> foundEntries = bundle.findEntries("OSGI-INF/blueprint", "*.xml", false);
            if (foundEntries != null)
            {
               while (foundEntries.hasMoreElements())
               {
                  String path = foundEntries.nextElement().toString();
                  int index = path.indexOf("OSGI-INF/blueprint");
                  pathList.add(path.substring(index));
               }
            }
         }

         if (pathList.isEmpty() == false)
         {
            log.debug("Create blueprint container");
            ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            try
            {
               Thread.currentThread().setContextClassLoader(BlueprintActivator.class.getClassLoader());
               BlueprintContainerImpl blueprintContainer = new BlueprintContainerImpl(bundle.getBundleContext(), context.getBundle(), eventDispatcher, handlers, executors, pathList);
               containers.put(bundle, blueprintContainer);
               blueprintContainer.schedule();
            }
            finally
            {
               Thread.currentThread().setContextClassLoader(tccl);
            }
         }
      }
      else if (state == Bundle.STOPPING)
      {
         BlueprintContainerImpl blueprintContainer = containers.remove(bundle);
         if (blueprintContainer != null)
         {
            log.debug("Stop blueprint container");
            blueprintContainer.destroy();
         }
      }
   }
}