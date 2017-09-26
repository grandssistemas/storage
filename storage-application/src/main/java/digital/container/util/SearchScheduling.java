package digital.container.util;

import digital.container.vo.TaxDocumentModel;

import java.io.Serializable;
import java.util.List;


public class SearchScheduling implements Serializable {
    private List<TaxDocumentScheduling> types;
    private List<String> cnpjs;

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
}
