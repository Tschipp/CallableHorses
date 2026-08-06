/*
 * GNU Lesser General Public License v3
 * Copyright (C) 2024 Tschipp
 * mrtschipp@gmail.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package tschipp.callablehorses.config;

import java.lang.reflect.Field;

public record PropertyData(Object fieldClass, Field field, AnnotationData data)
{
	public String getId()
	{
		return field().getName();
	}

	public boolean getBoolean() throws IllegalAccessException
	{
		return field().getBoolean(fieldClass());
	}

	public void setBoolean(boolean value)
	{
		try
		{
			field.setBoolean(fieldClass, value);
		}
		catch (Exception e)
		{
			//Ignore
		}
	}

	public int getInt() throws IllegalAccessException
	{
		return field().getInt(fieldClass());
	}

	public void setInt(int value)
	{
		try
		{
			field.setInt(fieldClass, value);
		}
		catch (Exception e)
		{
			//Ignore
		}
	}

	public double getDouble() throws IllegalAccessException
	{
		return field().getDouble(fieldClass());
	}

	public void setDouble(double value)
	{
		try
		{
			field.setDouble(fieldClass, value);
		}
		catch (Exception e)
		{
			//Ignore
		}
	}

	public String[] getStringArray() throws IllegalAccessException
	{
		return (String[]) field().get(fieldClass());
	}

	public void setStringArray(String[] arr)
	{
		try
		{
			field.set(fieldClass, arr);
		}
		catch (Exception e)
		{
			//Ignore
		}
	}
}
