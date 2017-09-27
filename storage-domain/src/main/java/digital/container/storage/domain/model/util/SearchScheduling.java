package digital.container.storage.domain.model.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class SearchScheduling implements Serializable {

    private String organizationCode;
    private List<TaxDocumentScheduling> types = new ArrayList<>();
    private List<String> cnpjs = new ArrayList<>();

    public List<String> getCnpjs() {
        return cnpjs;
    }

    public void setCnpjs(List<String> cnpjs) {
        this.cnpjs = cnpjs;
    }

    public List<TaxDocumentScheduling> getTypes() {
        return types;
    }

    public void setTypes(List<TaxDocumentScheduling> types) {
        this.types = types;
    }

    public void addTaxDocumentScheduling(TaxDocumentScheduling type) {
        this.types.add(type);
    }

    public void addCnpj(String cnpj) {
        this.cnpjs.add(cnpj);
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }
}
