//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.11.23 at 03:26:39 PM EET 
//


package eu.nimble.service.bp.hyperjaxb.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ProcessType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ProcessType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token"&gt;
 *     &lt;enumeration value="CATALOGUE"/&gt;
 *     &lt;enumeration value="NEGOTIATION"/&gt;
 *     &lt;enumeration value="ORDER"/&gt;
 *     &lt;enumeration value="REMITTANCEADVICE"/&gt;
 *     &lt;enumeration value="INVOICE"/&gt;
 *     &lt;enumeration value="TRACKING"/&gt;
 *     &lt;enumeration value="FULFILMENT"/&gt;
 *     &lt;enumeration value="PRODUCTCONFIGURATION"/&gt;
 *     &lt;enumeration value="TRANSPORT_EXECUTION_PLAN"/&gt;
 *     &lt;enumeration value="ITEM_INFORMATION_REQUEST"/&gt;
 *     &lt;enumeration value="OTHER"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ProcessType")
@XmlEnum
public enum ProcessType {

    CATALOGUE,
    NEGOTIATION,
    ORDER,
    REMITTANCEADVICE,
    INVOICE,
    TRACKING,
    FULFILMENT,
    PRODUCTCONFIGURATION,
    TRANSPORT_EXECUTION_PLAN,
    ITEM_INFORMATION_REQUEST,
    OTHER;

    public String value() {
        return name();
    }

    public static ProcessType fromValue(String v) {
        return valueOf(v);
    }

}
