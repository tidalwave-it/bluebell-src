/*
 * #%L
 * *********************************************************************************************************************
 *
 * blueBell
 * http://bluebell.tidalwave.it - hg clone https://bitbucket.org/tidalwave/bluebell-src
 * %%
 * Copyright (C) 2013 - 2013 Tidalwave s.a.s. (http://tidalwave.it)
 * %%
 * *********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * *********************************************************************************************************************
 *
 * $Id$
 *
 * *********************************************************************************************************************
 * #L%
 */
package it.tidalwave.bluebell.net.impl;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.io.StringReader;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/***********************************************************************************************************************
 *
 * @author  Fabrizio Giudici
 * @version $Id$
 *
 **********************************************************************************************************************/
@Slf4j @ToString(exclude = "parent")
public class XmlElement
  {
    public static final XmlElement NULL_ELEMENT = new XmlElement();

    @Getter @Setter
    protected String tagName;

    @Getter @Setter
    protected String value = "";

    protected final LinkedList<XmlElement> children = new LinkedList<XmlElement>();

    protected final Map<String, String> attributes = new HashMap<String, String>();

    @Getter @Setter
    protected XmlElement parent;

    /**
     * Returns the content value of this XML element as integer.
     *
     * @param defaultValue returned value if this content value cannot be
     *            converted into integer.
     * @return integer value of this content or default value indicated by the
     *         parameter.
     */
    public int getIntValue(int defaultValue) {
        if (value == null) {
            return defaultValue;
        } else {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    private void putAttribute(String name, String value) {
        attributes.put(name, value);
    }

    /**
     * Returns a value of attribute in this XML element.
     *
     * @param name attribute name
     * @param defaultValue returned value if a value of the attribute is not
     *            found.
     * @return a value of the attribute or the default value
     */
    public String getAttribute(String name, String defaultValue) {
        String ret = attributes.get(name);
        if (ret == null) {
            ret = defaultValue;
        }
        return ret;
    }

    /**
     * Returns a value of attribute in this XML element as integer.
     *
     * @param name attribute name
     * @param defaultValue returned value if a value of the attribute is not
     *            found.
     * @return a value of the attribute or the default value
     */
    public int getIntAttribute(String name, int defaultValue) {
        String attrValue = attributes.get(name);
        if (attrValue == null) {
            return defaultValue;
        } else {
            try {
                return Integer.valueOf(attrValue);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    private void putChild(XmlElement childItem) {
        children.add(childItem);
        childItem.setParent(this);
    }

    /**
     * Returns a child XML element. If a child element is not found, returns an
     * empty element instead of null.
     *
     * @param name name of child element
     * @return an element
     */
    public XmlElement findChild(String name) {

        for (final XmlElement child : children) {
            if (child.getTagName().equals(name)) {
                return child;
            }
        }
        return NULL_ELEMENT;
    }

    /**
     * Returns a list of child elements. If there is no child element, returns a
     * empty list instead of null.
     *
     * @param name name of child element
     * @return a list of child elements
     */
    public List<XmlElement> findChildren(String name) {
        final List<XmlElement> tagItemList = new ArrayList<XmlElement>();
        for (final XmlElement child : children) {
            if (child.getTagName().equals(name)) {
                tagItemList.add(child);
            }
        }
        return tagItemList;
    }

    /**
     * Checks to see whether this element is empty.
     *
     * @return true if empty, false otherwise.
     */
    public boolean isEmpty() {
        return (tagName == null);
    }

    /**
     * Parses XML data and returns the root element.
     *
     * @param parser parser
     * @return root element
     */
    @Nonnull
    public static XmlElement parse (final @Nonnull XmlPullParser parser)
      {
        XmlElement rootElement = XmlElement.NULL_ELEMENT;

        try
          {
            XmlElement parsingElement = XmlElement.NULL_ELEMENT;

MAINLOOP:   while (true)
              {
                switch (parser.next())
                  {
                    case XmlPullParser.START_DOCUMENT:
                        break;

                    case XmlPullParser.START_TAG:
                        final XmlElement childItem = new XmlElement();
                        childItem.setTagName(parser.getName());

                        if (parsingElement == XmlElement.NULL_ELEMENT)
                          {
                            rootElement = childItem;
                          }
                        else
                          {
                            parsingElement.putChild(childItem);
                          }

                        parsingElement = childItem;

                        for (int i = 0; i < parser.getAttributeCount(); i++)
                          {
                            parsingElement.putAttribute(parser.getAttributeName(i), parser.getAttributeValue(i));
                          }

                        break;

                    case XmlPullParser.TEXT:
                        parsingElement.setValue(parser.getText());
                        break;

                    case XmlPullParser.END_TAG:
                        parsingElement = parsingElement.getParent();
                        break;

                    case XmlPullParser.END_DOCUMENT:
                        break MAINLOOP;
                  }
              }
          }
        catch (final XmlPullParserException e)
          {
            log.error("parseXml: XmlPullParserException.", e);
            rootElement = XmlElement.NULL_ELEMENT;
          }
        catch (final IOException e)
          {
            log.error("parseXml: IOException.", e);
            rootElement = XmlElement.NULL_ELEMENT;
          }

        return rootElement;
      }

    /**
     * Parses XML data and returns the root element.
     *
     * @param xml XML data
     * @return root element
     */
    @Nonnull
    public static XmlElement parse (final @Nonnull String xml)
      {
        try
          {
            final XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            final XmlPullParser xmlPullParser = factory.newPullParser();
//            XmlPullParser xmlPullParser = Xml.newPullParser();
            xmlPullParser.setInput(new StringReader(xml));
            return parse(xmlPullParser);
          }
        catch (final XmlPullParserException e)
          {
            log.error("parseXml: XmlPullParserException occured.", e);
            return XmlElement.NULL_ELEMENT;
          }
      }
  }
