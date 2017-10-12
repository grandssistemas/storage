package digital.container.storage.domain.model.file;

import digital.container.storage.domain.model.util.LocalFileUtil;
import digital.container.storage.domain.model.util.TokenResultProxy;
import digital.container.storage.domain.model.util.TokenUtil;
import io.gumga.domain.GumgaSharedModelUUID;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Calendar;

@MappedSuperclass
public abstract class AbstractFile extends GumgaSharedModelUUID {

    @Column(name = "name")
    private String name;

    @Column(name = "nick_name")
    private String nickName;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="created")
    private Calendar createDate;

    @Column(name = "size")
    private Long size;

    @Column(name = "hash")
    private String hash;

    @Column(name = "container_key")
    private String containerKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_status")
    private FileStatus fileStatus;

    @Column(name = "file_public")
    private Boolean filePublic = Boolean.FALSE;

    @Enumerated(EnumType.STRING)
    @Column(name = "file_type")
    private FileType fileType;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "detail_one")
    private String detailOne;

    @Column(name = "detail_two")
    private String detailTwo;

    @Column(name = "detail_three")
    private String detailThree;

    @Column(name = "detail_four")
    private String detailFour;

    @Column(name = "relative_path")
    private String relativePath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Calendar getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Calendar createDate) {
        this.createDate = createDate;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public String getContainerKey() {
        return containerKey;
    }

    public void setContainerKey(String containerKey) {
        this.containerKey = containerKey;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getDetailOne() {
        return detailOne;
    }

    public void setDetailOne(String detailOne) {
        this.detailOne = detailOne;
    }

    public String getDetailTwo() {
        return detailTwo;
    }

    public void setDetailTwo(String detailTwo) {
        this.detailTwo = detailTwo;
    }

    public String getDetailThree() {
        return detailThree;
    }

    public void setDetailThree(String detailThree) {
        this.detailThree = detailThree;
    }

    public String getDetailFour() {
        return detailFour;
    }

    public void setDetailFour(String detailFour) {
        this.detailFour = detailFour;
    }

    public FileStatus getFileStatus() {
        return fileStatus;
    }

    public void setFileStatus(FileStatus fileStatus) {
        this.fileStatus = fileStatus;
    }

    public Boolean getFilePublic() {
        return filePublic;
    }

    public void setFilePublic(Boolean filePublic) {
        this.filePublic = filePublic;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public AbstractFile buildAnything(String fileName, String contentType, Long size, Boolean shared, String containerKey, TokenResultProxy tokenResultProxy) {
        setName(fileName);
        setFileStatus(FileStatus.DO_NOT_SYNC);
        setFileType(FileType.ANYTHING);

        setContentType(contentType);
        setSize(size);
        setFilePublic(shared);
        setCreateDate(Calendar.getInstance());

        setHash(getHashFile());
        setRelativePath(LocalFileUtil.getRelativePathFileANYTHING(containerKey) + '/' + getName());
        setContainerKey(containerKey);

        addSharing(tokenResultProxy);

        return this;
    }

    public AbstractFile buildTaxDocument(String contentType, Long size, Boolean shared, String containerKey, TokenResultProxy tokenResultProxy, LocalDate ld, FileType type) {
        addSharing(tokenResultProxy);

        setFilePublic(shared);
        setFileType(type);

        String path = LocalFileUtil.getRelativePathFileTAXDOCUMENT(containerKey,
                ld.getYear(),
                ld.getMonth().toString(),
                getFileType(),
                getDetailThree());

        setRelativePath(path.concat("/").concat(getName()));
        setHash(getHashFile());
        setContainerKey(containerKey);
        setCreateDate(Calendar.getInstance());
        setContentType(contentType);
        setSize(size);

        return this;
    }

    public AbstractFile buildTaxDocumentDisable(String infInutNProt, String infInutDhRecbto, String contentType, Long size, Boolean shared, String containerKey, TokenResultProxy tokenResultProxy, LocalDate ld, FileType type) {
        addSharing(tokenResultProxy);
        setFilePublic(shared);
        setFileType(type);
        setHash(getHashFile());
        setContainerKey(containerKey);
        setCreateDate(Calendar.getInstance());
        setContentType(contentType);
        setSize(size);
        setDetailOne(infInutNProt);
        setDetailTwo(infInutDhRecbto);

        String path = LocalFileUtil.getRelativePathFileTAXDOCUMENTDisable(containerKey,
                ld.getYear(),
                ld.getMonth().toString(),
                FileType.NFE_DISABLE.equals(getFileType()) ? FileType.NFE : FileType.NFCE);

        setRelativePath(path.concat("/").concat(getName()));

        return this;
    }

    public AbstractFile buildTaxDocumentLetterCorrection(String infInutDhRecbto, String chNFe, String containerKey, String contentType, Long size,  LocalDate ld, TokenResultProxy tokenResultProxy) {

        addSharing(tokenResultProxy);

        setDetailOne(chNFe);

        setFileType(FileType.NFE_LETTER_CORRECTION);

        setDetailTwo(infInutDhRecbto);

        String path = LocalFileUtil.getRelativePathFileTAXDOCUMENTLetterCorrection(containerKey,
                ld.getYear(),
                ld.getMonth().toString(),
                FileType.NFE);

        setHash(getHashFile());
        setRelativePath(path + "/" + getName());


        setContainerKey(containerKey);
        setCreateDate(Calendar.getInstance());
        setContentType(contentType);
        setSize(size);


        return this;
    }
    public AbstractFile buildTaxDocumentCanceled(String chNFe, String infEventochDhRegEvento, String movement, String containerKey, String contentType, Long size, LocalDate ld,  TokenResultProxy tokenResultProxy) {

        addSharing(tokenResultProxy);
        setDetailOne(chNFe);
        setDetailTwo(infEventochDhRegEvento);

        String path = LocalFileUtil.getRelativePathFileTAXDOCUMENTCanceled(containerKey,
                ld.getYear(),
                ld.getMonth().toString(),
                getFileType(),
                movement);

        setHash(getHashFile());
        setRelativePath(path.concat("/").concat(getName()));
        setContainerKey(containerKey);
        setCreateDate(Calendar.getInstance());


        setContentType(contentType);
        setSize(size);

        return this;
    }

    private void addSharing(TokenResultProxy  tokenResultProxy) {
        if(!TokenUtil.ACCOUNTANT_NO_HAVE_TOKEN.equals(tokenResultProxy.accountantOi)) {
            addOrganization(tokenResultProxy.accountantOi);
        }

        if(!TokenUtil.SOFTWARE_HOUSE_NO_HAVE_TOKEN.equals(tokenResultProxy.softwareHouseOi)) {
            addOrganization(tokenResultProxy.softwareHouseOi);
        }
    }

    protected abstract String getHashFile();
}
