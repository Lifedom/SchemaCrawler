/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2018, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

OperatingSystem is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

OperatingSystem and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/

package schemacrawler;


/**
 * JVM system information.
 *
 * @author Sualeh Fatehi sualeh@hotmail.com
 */
public final class JvmSystemInfo
  extends BaseProductVersion
{

  private static final long serialVersionUID = 4051323422934251828L;

  public JvmSystemInfo()
  {
    super(String.format("%s %s",
                        System.getProperty("java.vm.vendor", "<unknown>"),
                        System.getProperty("java.vm.name", "<unknown>")),
          System.getProperty("java.runtime.version", ""));
  }

}
