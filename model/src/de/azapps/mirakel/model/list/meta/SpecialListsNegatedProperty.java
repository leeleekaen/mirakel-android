/*******************************************************************************
 * Mirakel is an Android App for managing your ToDo-Lists
 *
 * Copyright (c) 2013-2014 Anatolij Zelenin, Georg Semmler.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package de.azapps.mirakel.model.list.meta;


public abstract class SpecialListsNegatedProperty extends
    SpecialListsBaseProperty {
    protected boolean done;

    abstract protected String propertyName();

    public SpecialListsNegatedProperty(final boolean done) {
        this.done = done;
    }

    public boolean getDone() {
        return this.done;
    }

    public void setDone(final boolean done) {
        this.done = done;
    }

    @Override
    public String serialize() {
        String ret = "\"" + propertyName() + "\":{";
        ret += "\"done\":" + (this.done ? "true" : "false");
        return ret + "}";
    }

}
