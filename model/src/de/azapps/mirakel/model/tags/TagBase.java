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
package de.azapps.mirakel.model.tags;

import android.content.ContentValues;
import android.graphics.Color;
import de.azapps.mirakel.model.DatabaseHelper;

class TagBase {

    public static final String DARK_TEXT = "dark_text";
    public static final String BACKGROUND_COLOR_R = "color_r";
    public static final String BACKGROUND_COLOR_B = "color_b";
    public static final String BACKGROUND_COLOR_G = "color_g";
    public static final String BACKGROUND_COLOR_A = "color_a";

    private boolean isDarkText;
    // Color as specified in android.graphics.Color
    private int backgroundColor;
    private String name;
    private int id;

    public TagBase(final int id, final boolean dark, final int backColor,
                   final String name) {
        setName(name);
        setId(id);
        setBackgroundColor(backColor);
        setDarkText(dark);
    }

    public ContentValues getContentValues() {
        final ContentValues cv = new ContentValues();
        cv.put(DatabaseHelper.ID, this.id);
        cv.put(DatabaseHelper.NAME, this.name);
        cv.put(TagBase.DARK_TEXT, this.isDarkText);
        cv.put(TagBase.BACKGROUND_COLOR_R, Color.red(this.backgroundColor));
        cv.put(TagBase.BACKGROUND_COLOR_G, Color.green(this.backgroundColor));
        cv.put(TagBase.BACKGROUND_COLOR_B, Color.blue(this.backgroundColor));
        cv.put(TagBase.BACKGROUND_COLOR_A, Color.alpha(this.backgroundColor));
        return cv;
    }

    public int getBackgroundColor() {
        return this.backgroundColor;
    }

    public void setBackgroundColor(final int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public boolean isDarkText() {
        return this.isDarkText;
    }

    public void setDarkText(final boolean isDarkText) {
        this.isDarkText = isDarkText;
    }

    public int getId() {
        return this.id;
    }

    protected void setId(final int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.id;
        result = prime * result + this.backgroundColor;
        result = prime * result + (this.isDarkText ? 1231 : 1237);
        result = prime * result
                 + (this.name == null ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TagBase)) {
            return false;
        }
        final TagBase other = (TagBase) obj;
        if (this.id != other.id) {
            return false;
        }
        if (this.backgroundColor != other.backgroundColor) {
            return false;
        }
        if (this.isDarkText != other.isDarkText) {
            return false;
        }
        if (this.name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!this.name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
