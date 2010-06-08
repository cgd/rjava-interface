/*
 * Copyright (c) 2010 The Jackson Laboratory
 * 
 * This software was developed by Gary Churchill's Lab at The Jackson
 * Laboratory (see http://research.jax.org/faculty/churchill).
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jax.r.jriutilities;

/**
 * the bioconductor package dependency
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
 */
public class BioconductorPackageDependency extends RPackageDependency
{
    /**
     * Constructor
     * @param rInterface        the R interface
     * @param packageName       the package name of this dependency
     * @param minimumVersion    the minimum version that we'll allow
     */
    public BioconductorPackageDependency(
            RInterface rInterface,
            String packageName,
            String minimumVersion)
    {
        super(rInterface, packageName, minimumVersion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void installPackage()
    {
        this.getRInterface().evaluateCommand(
                "source(\"http://bioconductor.org/biocLite.R\")");
        this.getRInterface().evaluateCommand(
                "biocLite(\"" + this.getPackageName() + "\")");
    }
}
