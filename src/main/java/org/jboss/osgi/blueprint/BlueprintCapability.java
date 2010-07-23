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
package org.jboss.osgi.blueprint;

//$Id$

import org.jboss.osgi.spi.capability.Capability;
import org.jboss.osgi.testing.OSGiRuntime;

/**
 * Adds the Blueprint capability to the {@link OSGiRuntime}
 * under test. 
 * 
 * It is ignored if the {@link BlueprintService} is already registered.
 * 
 * Installed bundles: jbosgi-blueprint.jar, org.apache.aries.blueprint.jar  
 * 
 * @author thomas.diesler@jboss.com
 * @since 05-May-2009
 */
public class BlueprintCapability extends Capability
{
   public BlueprintCapability()
   {
      super(BlueprintService.class.getName());

      addBundle("bundles/org.apache.aries.util.jar");
      addBundle("bundles/jbosgi-blueprint.jar");
   }
}