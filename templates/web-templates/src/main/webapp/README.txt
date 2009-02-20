====

    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have recieved a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license

    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.
====

folder description :

first level : jsp files are TEMPLATES files + definitions (.cnd) and ordering instructions for fields in engines (.grp)
    main file is positioning.jsp

common/ :
    all common files called in most templates (declarations, headers, footers, navbars, etc.) or specific features pre-packaged that aree not templates (login form, serach form...)

areas/ :

    one folder for each template, within one file for each area

skins/ :
    one folder for each skin, skins are layouts applied on skinnable containers. skinnable is a container property

theme/ :
    one folder for each graphic theme, contains css folder where all css files are loaded + js scripts specific to the theme and images.

modules/ :
    contains all display files for each container or content module.

images :
    preview.gif (710x141) preview in template choose from site wizzard