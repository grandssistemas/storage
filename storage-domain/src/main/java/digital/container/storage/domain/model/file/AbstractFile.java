package digital.container.storage.domain.model.file;

import digital.container.storage.domain.model.util.LocalFileUtil;
import io.gumga.domain.GumgaSharedModelUUID;

import javax.persistence.*;
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

    public AbstractFile buildAnything(String fileName, String contentType, Long size, Boolean shared, String containerKey) {
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

        return this;
    }

    protected abstract String getHashFile();
}
