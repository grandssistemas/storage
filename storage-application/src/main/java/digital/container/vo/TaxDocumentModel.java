package digital.container.vo;


import digital.container.storage.domain.model.file.FileType;

public class TaxDocumentModel {

    public String model = "DEFAULT";

    public Boolean isValid() {
        return (model.equals("55") || model.equals("65"));
    }

    public FileType getFileType() {
        switch (model) {
            case "55": return FileType.NFE;
            case "65": return FileType.NFCE;
            default: return FileType.INVALID_TAX_DOCUMENT;
        }
    }

}
