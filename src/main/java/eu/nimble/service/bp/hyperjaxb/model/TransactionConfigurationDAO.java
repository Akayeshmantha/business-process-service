//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.11 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.11.23 at 03:26:39 PM EET 
//


package eu.nimble.service.bp.hyperjaxb.model;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.jvnet.jaxb2_commons.lang.Equals;
import org.jvnet.jaxb2_commons.lang.EqualsStrategy;
import org.jvnet.jaxb2_commons.lang.JAXBEqualsStrategy;
import org.jvnet.jaxb2_commons.locator.ObjectLocator;
import org.jvnet.jaxb2_commons.locator.util.LocatorUtils;


/**
 * <p>Java class for TransactionConfigurationDAO complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TransactionConfigurationDAO"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="transactionID" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="executionConfigurations" type="{}ExecutionConfigurationDAO" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TransactionConfigurationDAO", propOrder = {
    "transactionID",
    "executionConfigurations"
})
@Entity(name = "TransactionConfigurationDAO")
@Table(name = "TRANSACTION_CONFIGURATION_DAO")
@Inheritance(strategy = InheritanceType.JOINED)
public class TransactionConfigurationDAO
    implements Equals
{

    @XmlElement(required = true)
    protected String transactionID;
    @XmlElement(required = true)
    protected List<ExecutionConfigurationDAO> executionConfigurations;
    @XmlAttribute(name = "Hjid")
    protected Long hjid;

    /**
     * Gets the value of the transactionID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Basic
    @Column(name = "TRANSACTION_ID", length = 255)
    public String getTransactionID() {
        return transactionID;
    }

    /**
     * Sets the value of the transactionID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionID(String value) {
        this.transactionID = value;
    }

    /**
     * Gets the value of the executionConfigurations property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the executionConfigurations property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExecutionConfigurations().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ExecutionConfigurationDAO }
     * 
     * 
     */
    @OneToMany(targetEntity = ExecutionConfigurationDAO.class, cascade = {
        CascadeType.ALL
    })
    @JoinColumn(name = "EXECUTION_CONFIGURATIONS_TRA_0")
    public List<ExecutionConfigurationDAO> getExecutionConfigurations() {
        if (executionConfigurations == null) {
            executionConfigurations = new ArrayList<ExecutionConfigurationDAO>();
        }
        return this.executionConfigurations;
    }

    /**
     * 
     * 
     */
    public void setExecutionConfigurations(List<ExecutionConfigurationDAO> executionConfigurations) {
        this.executionConfigurations = executionConfigurations;
    }

    public boolean equals(ObjectLocator thisLocator, ObjectLocator thatLocator, Object object, EqualsStrategy strategy) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final TransactionConfigurationDAO that = ((TransactionConfigurationDAO) object);
        {
            String lhsTransactionID;
            lhsTransactionID = this.getTransactionID();
            String rhsTransactionID;
            rhsTransactionID = that.getTransactionID();
            if (!strategy.equals(LocatorUtils.property(thisLocator, "transactionID", lhsTransactionID), LocatorUtils.property(thatLocator, "transactionID", rhsTransactionID), lhsTransactionID, rhsTransactionID)) {
                return false;
            }
        }
        {
            List<ExecutionConfigurationDAO> lhsExecutionConfigurations;
            lhsExecutionConfigurations = (((this.executionConfigurations!= null)&&(!this.executionConfigurations.isEmpty()))?this.getExecutionConfigurations():null);
            List<ExecutionConfigurationDAO> rhsExecutionConfigurations;
            rhsExecutionConfigurations = (((that.executionConfigurations!= null)&&(!that.executionConfigurations.isEmpty()))?that.getExecutionConfigurations():null);
            if (!strategy.equals(LocatorUtils.property(thisLocator, "executionConfigurations", lhsExecutionConfigurations), LocatorUtils.property(thatLocator, "executionConfigurations", rhsExecutionConfigurations), lhsExecutionConfigurations, rhsExecutionConfigurations)) {
                return false;
            }
        }
        return true;
    }

    public boolean equals(Object object) {
        final EqualsStrategy strategy = JAXBEqualsStrategy.INSTANCE;
        return equals(null, null, object, strategy);
    }

    /**
     * Gets the value of the hjid property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    @Id
    @Column(name = "HJID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getHjid() {
        return hjid;
    }

    /**
     * Sets the value of the hjid property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setHjid(Long value) {
        this.hjid = value;
    }

}
