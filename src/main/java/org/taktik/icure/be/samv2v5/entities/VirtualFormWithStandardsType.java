//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.10.15 at 03:32:18 PM CEST 
//


package org.taktik.icure.be.samv2v5.entities;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for VirtualFormWithStandardsType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="VirtualFormWithStandardsType">
 *   &lt;complexContent>
 *     &lt;extension base="{urn:be:fgov:ehealth:samws:v2:refdata}VirtualFormType">
 *       &lt;sequence>
 *         &lt;element name="StandardForm" type="{urn:be:fgov:ehealth:samws:v2:export}ExportStandardFormType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VirtualFormWithStandardsType", propOrder = {
    "standardForm"
})
public class VirtualFormWithStandardsType
    extends VirtualFormType
{

    @XmlElement(name = "StandardForm")
    protected List<ExportStandardFormType> standardForm;

    /**
     * Gets the value of the standardForm property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the standardForm property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStandardForm().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExportStandardFormType }
     * 
     * 
     */
    public List<ExportStandardFormType> getStandardForm() {
        if (standardForm == null) {
            standardForm = new ArrayList<ExportStandardFormType>();
        }
        return this.standardForm;
    }

}