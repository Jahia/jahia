/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

// Attach the filter to a table. filterRow specifies the rownumber at which the filter should be inserted.
function attachFilter(table, filterRow)
{
	table.filterRow = filterRow;
	// Check if the table has any rows. If not, do nothing
	if(table.rows.length > 0)
	{
		// Insert the filterrow and add cells whith drowdowns.
		var filterRow = table.insertRow(table.filterRow);
		for(var i = 0; i < table.rows[table.filterRow + 1].cells.length; i++)
		{
			var c = document.createElement("TH");
			table.rows[table.filterRow].appendChild(c);
			var opt = document.createElement("select");
			opt.onchange = filter;

			c.appendChild(opt);
		}
		// Set the functions
		table.fillFilters = fillFilters;
		table.inFilter = inFilter;
		table.buildFilter = buildFilter;
		table.showAll = showAll;
		table.detachFilter = detachFilter;
		table.filterElements = new Array();
		
		// Fill the filters
		table.fillFilters();
		table.filterEnabled = true;
	}
}

function detachFilter()
{
	if(this.filterEnabled)
	{
		// Remove the filter
		this.showAll();
		this.deleteRow(this.filterRow);
		this.filterEnabled = false;
	}
}

// Checks if a column is filtered
function inFilter(col)
{
	for(var i = 0; i < this.filterElements.length; i++)
	{
		if(this.filterElements[i].index == col)
			return true;
	}
	return false;
}

// Fills the filters for columns which are not fiiltered
function fillFilters()
{
	for(var col = 0; col < this.rows[this.filterRow].cells.length; col++)
	{
		if(!this.inFilter(col))
		{
			this.buildFilter(col, "(all)");
		}
	}
}

// Fills the columns dropdown box. 
// setValue is the value which the dropdownbox should have one filled. 
// If the value is not suplied, the first item is selected
function buildFilter(col, setValue)
{
	// Get a reference to the dropdownbox.
	var opt = this.rows[this.filterRow].cells[col].firstChild;
	
	// remove all existing items
	while(opt.length > 0)
		opt.remove(0);
	
	var values = new Array();
		
	// put all relevant strings in the values array.
	for(var i = this.filterRow + 1; i < this.rows.length; i++)
	{
		var row = this.rows[i];
		if(row.style.display != "none" && row.className != "noFilter")
		{
			values.push(row.cells[col].innerHTML.toLowerCase());
		}
	}
	values.sort();
	
	//add each unique string to the dopdownbox
	var value;
	for(var i = 0; i < values.length; i++)
	{
		if(values[i].toLowerCase() != value)
		{
			value = values[i].toLowerCase();
			opt.options.add(new Option(values[i], value));
		}
	}

	opt.options.add(new Option("(all)", "(all)"), 0);

	if(setValue != undefined)
		opt.value = setValue;
	else
		opt.options[0].selected = true;
}

// This function is called when a dropdown box changes
function filter()
{
	var table = this; // 'this' is a reference to the dropdownbox which changed
	while(table.tagName.toUpperCase() != "TABLE")
		table = table.parentNode;

	var filterIndex = this.parentNode.cellIndex; // The column number of the column which should be filtered
	var filterText = table.rows[table.filterRow].cells[filterIndex].firstChild.value;
	
	// First check if the column is allready in the filter.
	var bFound = false;
	
	for(var i = 0; i < table.filterElements.length; i++)
	{
		if(table.filterElements[i].index == filterIndex)
		{
			bFound = true;
			// If the new value is '(all') this column is removed from the filter.
			if(filterText == "(all)")
			{
				table.filterElements.splice(i, 1);
			}
			else
			{
				table.filterElements[i].filter = filterText;
			}
			break;
		}
	}
	if(!bFound)
	{
		// the column is added to the filter
		var obj = new Object();
		obj.filter = filterText;
		obj.index = filterIndex;
		table.filterElements.push(obj);
	}
	
	// first set all rows to be displayed
	table.showAll();
	
	// the filter ou the right rows.
	for(var i = 0; i < table.filterElements.length; i++)
	{
		// First fill the dropdown box for this column
		table.buildFilter(table.filterElements[i].index, table.filterElements[i].filter);
		// Apply the filter
		for(var j = table.filterRow + 1; j < table.rows.length; j++)
		{
			var row = table.rows[j];
			
			if(table.style.display != "none" && row.className != "noFilter")
			{
				if(table.filterElements[i].filter != row.cells[table.filterElements[i].index].innerHTML.toLowerCase())
				{
					row.style.display = "none";
				}
			}
		}
	}
	// Fill the dropdownboxes for the remaining columns.
	table.fillFilters();
}

function showAll()
{
	for(var i = this.filterRow + 1; i < this.rows.length; i++)
	{
		this.rows[i].style.display = "";
	}
}
