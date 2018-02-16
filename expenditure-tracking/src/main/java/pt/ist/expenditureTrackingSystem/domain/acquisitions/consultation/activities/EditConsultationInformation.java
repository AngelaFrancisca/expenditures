package pt.ist.expenditureTrackingSystem.domain.acquisitions.consultation.activities;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

import module.workflow.activities.ActivityInformation;
import module.workflow.activities.WorkflowActivity;
import module.workflow.domain.WorkflowProcess;
import pt.ist.expenditureTrackingSystem.domain.ContractType;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.Material;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.consultation.MultipleSupplierConsultation;
import pt.ist.expenditureTrackingSystem.domain.acquisitions.consultation.MultipleSupplierConsultationProcess;
import pt.ist.expenditureTrackingSystem.domain.organization.Person;

public class EditConsultationInformation extends ActivityInformation<MultipleSupplierConsultationProcess> {

    private String description;
    private Material material;
    private String justification;
    private ContractType contractType;
    private Integer contractDuration;
    private Person contractManager;
    private String supplierCountJustification;
    private LocalDate proposalDeadline;
    private Integer proposalValidity;
    private BigDecimal collateral;

    public EditConsultationInformation(final MultipleSupplierConsultationProcess process,
            final WorkflowActivity<? extends WorkflowProcess, ? extends ActivityInformation> activity) {
        super(process, activity);
        final MultipleSupplierConsultation consultation = process.getConsultation();
        setDescription(consultation.getDescription());
        setMaterial(consultation.getMaterial());
        setJustification(consultation.getJustification());
        setContractType(consultation.getContractType());
        setContractDuration(consultation.getContractDuration());
        if (consultation.getContractManager() != null) {
            setContractManager(consultation.getContractManager().getExpenditurePerson());
        }
        setSupplierCountJustification(consultation.getSupplierCountJustification());
        setProposalDeadline(consultation.getProposalDeadline());
        setProposalValidity(consultation.getProposalValidity());
        setCollateral(consultation.getCollateral());
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(String justification) {
        this.justification = justification;
    }

    public ContractType getContractType() {
        return contractType;
    }

    public void setContractType(ContractType contractType) {
        this.contractType = contractType;
    }

    public Integer getContractDuration() {
        return contractDuration;
    }

    public void setContractDuration(Integer contractDuration) {
        this.contractDuration = contractDuration;
    }

    public Person getContractManager() {
        return contractManager;
    }

    public void setContractManager(Person contractManager) {
        this.contractManager = contractManager;
    }

    public String getSupplierCountJustification() {
        return supplierCountJustification;
    }

    public void setSupplierCountJustification(String supplierCountJustification) {
        this.supplierCountJustification = supplierCountJustification;
    }

    public LocalDate getProposalDeadline() {
        return proposalDeadline;
    }

    public void setProposalDeadline(LocalDate proposalDeadline) {
        this.proposalDeadline = proposalDeadline;
    }

    public Integer getProposalValidity() {
        return proposalValidity;
    }

    public void setProposalValidity(Integer proposalValidity) {
        this.proposalValidity = proposalValidity;
    }

    public BigDecimal getCollateral() {
        return collateral;
    }

    public void setCollateral(BigDecimal collateral) {
        this.collateral = collateral;
    }

    @Override
    public boolean hasAllneededInfo() {
        return isForwardedFromInput();
    }

}
